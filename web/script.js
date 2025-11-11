// ---------- Cadastrar Pet ----------
const form = document.getElementById("formPet");
if (form) {
  form.addEventListener("submit", async (e) => {
    e.preventDefault();

    const dados = {
      nome: form.nome.value,
      especie: form.especie.value,
      idade: parseInt(form.idade.value),
      tutor: form.tutor.value
    };

await fetch("/api/pets", {
  method: "POST",
  headers: {
    "Content-Type": "application/json"   // 🔹 avisa ao Java que é JSON
  },
  body: JSON.stringify(dados)
});

alert("🐶 Pet cadastrado com sucesso!");
form.reset();

  });
}

// ---------- Listar Pets ----------
const tabela = document.getElementById("tabelaPets");
if (tabela) {
  fetch("/api/pets")
    .then(res => res.json())
    .then(pets => {
      pets.forEach(p => {
        const linha = document.createElement("tr");
        linha.innerHTML = `
          <td>${p.nome}</td>
          <td>${p.especie}</td>
          <td>${p.idade}</td>
          <td>${p.tutor}</td>
          <td><button onclick="excluir(${p.id})">❌</button></td>
        `;
        tabela.appendChild(linha);
      });
    });
}

// ---------- Excluir Pet ----------
async function excluir(id) {
  if (confirm("Tem certeza que deseja excluir este pet?")) {
    await fetch(`/api/pets?id=${id}`, { method: "DELETE" });
    alert("🐾 Pet removido!");
    location.reload();
  }
}
