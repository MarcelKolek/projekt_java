let page = 0;
let size = 10;

async function fetchJson(url, options = {}) {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options
  });

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch { /* ignore */ }

  if (!res.ok) {
    let msg =
      (data && data.message) ? data.message :
      (data && data.error) ? data.error :
      text || `HTTP ${res.status}`;

    if (data && data.errors) {
      const parts = [];
      for (const [k, v] of Object.entries(data.errors)) parts.push(`${k}: ${v}`);
      if (parts.length) msg = parts.join(", ");
    }

    throw new Error(msg);
  }

  return data;
}

function qs(params) {
  const p = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== null && v !== undefined && String(v).trim() !== "") p.set(k, v);
  });
  return p.toString();
}

async function loadCategories(selectedTaskCategoryId = null) {
  const categories = await fetchJson("/api/v1/categories");
  const filter = document.getElementById("filterCategory");
  const formSel = document.getElementById("taskCategory");

  filter.innerHTML = `<option value="">(wszystkie)</option>`;
  formSel.innerHTML = `<option value="">(brak)</option>`;

  for (const c of categories) {
    filter.insertAdjacentHTML("beforeend", `<option value="${c.id}">${escapeHtml(c.name)}</option>`);
    formSel.insertAdjacentHTML("beforeend", `<option value="${c.id}">${escapeHtml(c.name)}</option>`);
  }

  if (selectedTaskCategoryId !== null && selectedTaskCategoryId !== undefined) {
    formSel.value = String(selectedTaskCategoryId);
  }
}

function renderTasks(pageObj) {
  const tbody = document.getElementById("tasksTbody");
  const content = pageObj.content || [];

  if (content.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-muted">Brak wyników</td></tr>`;
  } else {
    tbody.innerHTML = content.map(t => {
      let statusLabel = t.status;
      let badgeClass = "bg-secondary";
      if (t.status === "TODO") {
        statusLabel = "Do zrobienia";
        badgeClass = "bg-secondary";
      } else if (t.status === "IN_PROGRESS") {
        statusLabel = "W toku";
        badgeClass = "bg-warning text-dark";
      } else if (t.status === "DONE") {
        statusLabel = "Zrobione";
        badgeClass = "bg-success";
      }

      return `
      <tr>
        <td>${t.id}</td>
        <td>${escapeHtml(t.title)}</td>
        <td><span class="badge ${badgeClass}">${statusLabel}</span></td>
        <td>${t.dueDate ?? ""}</td>
        <td>${renderCategoryBadge(t.category)}</td>
        <td>${renderThumbnail(t.attachmentFilename)}</td>
        <td class="d-flex gap-2">
          <button class="btn btn-sm btn-outline-primary" onclick="openEdit(${t.id})">Edytuj</button>
          <button class="btn btn-sm btn-outline-danger" onclick="removeTask(${t.id})">Usuń</button>
        </td>
      </tr>
    `;
    }).join("");
  }

  document.getElementById("pageInfo").textContent =
    `Strona ${pageObj.number + 1} / ${Math.max(pageObj.totalPages, 1)} • elementów: ${pageObj.totalElements}`;
}

function renderThumbnail(filename) {
  if (!filename) return "";
  const ext = filename.split(".").pop().toLowerCase();
  const isImage = ["jpg", "jpeg", "png", "gif", "webp"].includes(ext);
  
  if (isImage) {
    return `<a href="/api/v1/tasks/download/${filename}" target="_blank">
              <img src="/api/v1/tasks/download/${filename}" alt="thumb" style="width:50px; height:50px; object-fit:cover; border-radius:4px;">
            </a>`;
  }
  return `<a href="/api/v1/tasks/download/${filename}" class="small">Plik</a>`;
}

function escapeHtml(s) {
  return String(s ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

/* ====== category color helpers ====== */

function sanitizeHex(hex) {
  if (!hex) return null;
  const s = String(hex).trim();
  return /^#([0-9a-fA-F]{6})$/.test(s) ? s : null;
}

function renderCategoryBadge(category) {
  if (!category) return "";
  const name = escapeHtml(category.name ?? "");
  const color = sanitizeHex(category.color);

  if (!color) return name;

  return `
    <span class="badge rounded-pill"
          style="color:${color}; border:1px solid ${color}; background: transparent;">
      ${name}
    </span>
  `;
}

/* ====== tasks ====== */

async function loadTasks() {
  const status = document.getElementById("filterStatus").value;
  const categoryId = document.getElementById("filterCategory").value;
  const q = document.getElementById("filterQ").value;
  const dueBefore = document.getElementById("filterDueBefore").value;
  const dueAfter = document.getElementById("filterDueAfter").value;

  const query = qs({ status, categoryId, q, dueBefore, dueAfter, page, size, sort: "dueDate,asc" });
  const data = await fetchJson(`/api/v1/tasks?${query}`);
  renderTasks(data);
}

async function loadStats() {
  const s = await fetchJson("/api/v1/tasks/stats/jdbc"); 
  document.getElementById("statTotal").textContent = s.total;
  document.getElementById("statTodo").textContent = s.todo;
  document.getElementById("statInProgress").textContent = s.inProgress;
  document.getElementById("statDone").textContent = s.done;
  document.getElementById("statPercent").textContent = `Ukończono: ${Number(s.percentDone).toFixed(1)}%`;
}

function openNew() {
  document.getElementById("formError").classList.add("d-none");
  document.getElementById("taskModalTitle").textContent = "Nowe zadanie";
  document.getElementById("taskId").value = "";
  document.getElementById("taskTitle").value = "";
  document.getElementById("taskDescription").value = "";
  document.getElementById("taskStatus").value = "TODO";
  document.getElementById("taskDueDate").value = "";
  document.getElementById("taskCategory").value = "";
  
  document.getElementById("uploadArea").classList.remove("d-none");
  document.getElementById("btnUpload").classList.add("d-none");
  document.getElementById("taskFile").value = "";
  document.getElementById("attachmentArea").classList.add("d-none");
  
  bootstrap.Modal.getOrCreateInstance(document.getElementById("taskModal")).show();
}

async function openEdit(id) {
  const t = await fetchJson(`/api/v1/tasks/${id}`);
  document.getElementById("formError").classList.add("d-none");
  document.getElementById("taskModalTitle").textContent = `Edycja zadania #${id}`;
  document.getElementById("taskId").value = t.id;
  document.getElementById("taskTitle").value = t.title ?? "";
  document.getElementById("taskDescription").value = t.description ?? "";
  document.getElementById("taskStatus").value = t.status ?? "TODO";
  document.getElementById("taskDueDate").value = t.dueDate ?? "";
  document.getElementById("taskCategory").value = t.category ? String(t.category.id) : "";
  
  document.getElementById("uploadArea").classList.remove("d-none");
  document.getElementById("btnUpload").classList.remove("d-none");
  document.getElementById("taskFile").value = "";
  
  const attachArea = document.getElementById("attachmentArea");
  if (t.attachmentFilename) {
    attachArea.classList.remove("d-none");
    document.getElementById("attachmentName").textContent = t.attachmentFilename;
    document.getElementById("btnDownload").href = `/api/v1/tasks/download/${t.attachmentFilename}`;
  } else {
    attachArea.classList.add("d-none");
  }

  bootstrap.Modal.getOrCreateInstance(document.getElementById("taskModal")).show();
}

async function uploadFile() {
  const taskId = document.getElementById("taskId").value;
  const fileInput = document.getElementById("taskFile");
  if (!fileInput.files.length) {
    alert("Wybierz plik!");
    return;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);
  formData.append("taskId", taskId);

  try {
    const res = await fetch("/api/v1/tasks/upload", {
      method: "POST",
      body: formData
    });
    if (!res.ok) throw new Error(await res.text());
    
    alert("Plik wgrany pomyślnie!");
    openEdit(taskId); // odśwież modal
  } catch (e) {
    alert("Błąd uploadu: " + e.message);
  }
}

async function removeAttachment() {
  const taskId = document.getElementById("taskId").value;
  if (!taskId) return;
  if (!confirm("Czy na pewno chcesz usunąć załącznik?")) return;

  try {
    const res = await fetch(`/api/v1/tasks/${taskId}/attachment`, {
      method: "DELETE"
    });
    if (!res.ok) throw new Error(await res.text());
    
    alert("Załącznik usunięty!");
    openEdit(taskId); // refresh modal
    await loadTasks(); // refresh list
  } catch (e) {
    alert("Błąd usuwania załącznika: " + e.message);
  }
}

async function saveTask() {
  const id = document.getElementById("taskId").value || null;
  const payload = {
    title: document.getElementById("taskTitle").value,
    description: document.getElementById("taskDescription").value,
    status: document.getElementById("taskStatus").value,
    dueDate: document.getElementById("taskDueDate").value || null,
    categoryId: document.getElementById("taskCategory").value
      ? Number(document.getElementById("taskCategory").value)
      : null
  };

  const fileInput = document.getElementById("taskFile");
  const formData = new FormData();
  formData.append("task", new Blob([JSON.stringify(payload)], { type: "application/json" }));
  if (fileInput.files.length > 0) {
    formData.append("file", fileInput.files[0]);
  }

  try {
    if (!id) {
      await fetch("/api/v1/tasks", { 
        method: "POST", 
        body: formData 
      }).then(res => {
        if (!res.ok) return res.text().then(t => { throw new Error(t) });
        return res.json();
      });
    } else {
      await fetch(`/api/v1/tasks/${id}`, { 
        method: "PUT", 
        body: formData 
      }).then(res => {
        if (!res.ok) return res.text().then(t => { throw new Error(t) });
        return res.json();
      });
    }
    bootstrap.Modal.getOrCreateInstance(document.getElementById("taskModal")).hide();
    await loadTasks();
    await loadStats();
  } catch (e) {
    const box = document.getElementById("formError");
    box.textContent = e.message;
    box.classList.remove("d-none");
  }
}

async function removeTask(id) {
  if (!confirm(`Usunąć task #${id}?`)) return;
  await fetchJson(`/api/v1/tasks/${id}`, { method: "DELETE" });
  await loadTasks();
  await loadStats();
}

async function deleteCategory() {
  const catId = document.getElementById("filterCategory").value;
  if (!catId) {
    alert("Wybierz kategorię do usunięcia w filtrze!");
    return;
  }
  if (!confirm("Czy na pewno chcesz usunąć tę kategorię? Zadania zostaną zachowane, ale nie będą miały przypisanej kategorii.")) return;

  try {
    await fetchJson(`/api/v1/categories/${catId}`, { method: "DELETE" });
    await loadCategories();
    await loadTasks();
  } catch (e) {
    alert("Błąd podczas usuwania kategorii: " + e.message);
  }
}

/* =========================
   Category modal (REST)
   ========================= */

function resetCatError() {
  const box = document.getElementById("catError");
  box.classList.add("d-none");
  box.textContent = "";
}

function getSelectedPresetColor() {
  const el = document.querySelector('input[name="catColorPreset"]:checked');
  return el ? el.value : "#0d6efd";
}

function setPreviewColor(hex) {
  const prev = document.getElementById("catColorPreview");
  if (prev) prev.style.background = hex || "#0d6efd";
}

function getFinalCategoryColor() {
  const custom = document.getElementById("catColorCustom").value.trim();
  if (custom === "") return getSelectedPresetColor();

  const normalized = custom.startsWith("#") ? custom : `#${custom}`;
  const ok = sanitizeHex(normalized);
  if (!ok) throw new Error("Niepoprawny HEX. Użyj formatu #RRGGBB (np. #0d6efd).");
  return ok;
}

async function createCategory() {
  resetCatError();

  const name = document.getElementById("catName").value.trim();
  if (!name) {
    const box = document.getElementById("catError");
    box.textContent = "Nazwa kategorii nie może być pusta.";
    box.classList.remove("d-none");
    return;
  }

  let color;
  try {
    color = getFinalCategoryColor();
  } catch (e) {
    const box = document.getElementById("catError");
    box.textContent = e.message;
    box.classList.remove("d-none");
    return;
  }

  try {
    const created = await fetchJson("/api/v1/categories", {
      method: "POST",
      body: JSON.stringify({ name, color })
    });

    await loadCategories(created.id);

    document.getElementById("catName").value = "";
    document.getElementById("catColorCustom").value = "";

    const blue = document.querySelector('input[name="catColorPreset"][value="#0d6efd"]');
    if (blue) blue.checked = true;

    setPreviewColor("#0d6efd");

    bootstrap.Modal.getOrCreateInstance(document.getElementById("categoryModal")).hide();
  } catch (e) {
    const box = document.getElementById("catError");
    box.textContent = e.message;
    box.classList.remove("d-none");
  }
}

// eventy
window.addEventListener("DOMContentLoaded", async () => {
  document.getElementById("btnApply").addEventListener("click", async () => { page = 0; await loadTasks(); });
  document.getElementById("btnNew").addEventListener("click", openNew);
  document.getElementById("btnSave").addEventListener("click", saveTask);

  document.getElementById("btnPrev").addEventListener("click", async () => {
    if (page > 0) { page--; await loadTasks(); }
  });

  document.getElementById("btnNext").addEventListener("click", async () => {
    page++; await loadTasks();
  });

  document.getElementById("btnCreateCategory").addEventListener("click", createCategory);
  document.getElementById("btnDeleteCategory").addEventListener("click", deleteCategory);
  document.getElementById("btnUpload").addEventListener("click", uploadFile);
  document.getElementById("btnRemoveAttachment").addEventListener("click", removeAttachment);

  document.getElementById("categoryModal").addEventListener("show.bs.modal", () => {
    resetCatError();
    setPreviewColor(getSelectedPresetColor());
  });

  document.getElementById("catPresetColors").addEventListener("change", () => {
    const preset = getSelectedPresetColor();
    const custom = document.getElementById("catColorCustom").value.trim();
    const normalized = custom ? (custom.startsWith("#") ? custom : `#${custom}`) : "";
    setPreviewColor(sanitizeHex(normalized) || preset);
  });

  document.getElementById("catColorCustom").addEventListener("input", () => {
    const custom = document.getElementById("catColorCustom").value.trim();
    const normalized = custom ? (custom.startsWith("#") ? custom : `#${custom}`) : "";
    setPreviewColor(sanitizeHex(normalized) || getSelectedPresetColor());
  });

  await loadCategories();
  await loadTasks();
  await loadStats();
});

// potrzebne, bo onclick z HTML
window.openEdit = openEdit;
window.removeTask = removeTask;
