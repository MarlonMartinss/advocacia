# Teste do parcelamento – Saldo a pagar (Negócio)

Acesse **http://localhost:4200/contratos** e siga os passos abaixo.

---

## 1. Chegar na etapa Negócio

- Clique em **Novo contrato** (ou edite um existente).
- Avance até a **Página 4: Negócio** (pode preencher o mínimo nas páginas 1–3 para poder avançar).

---

## 2. Habilitar o parcelamento (redução do saldo)

- **Valor total do negócio (R$):** ex.: `100000`
- Deixe **Imóvel em permuta** e **Veículo em permuta** em branco ou zero.
- No **Resumo do negócio** deve aparecer **Saldo a pagar (R$) = R$ 100.000,00**.

Depois **reduza** o saldo a pagar (o gatilho é “saldo atual < saldo anterior”):

- **Valor do imóvel dado em permuta (R$):** ex.: `20000`  
  → Saldo a pagar passa a **R$ 80.000,00**.

Resultado esperado:

- Os campos **Nº de parcelas** e **Data da 1ª parcela** são **habilitados**.
- A seção **Parcelas do saldo a pagar** aparece (tabela pode ainda estar vazia).

---

## 3. Gerar a lista de parcelas

- **Nº de parcelas:** ex.: `4`
- **Data da 1ª parcela:** ex.: uma data futura (ex.: 15/03/2025).
- **Vencimentos:** ex.: `Todo dia 10` ou só `10`.

Resultado esperado:

- Tabela **Parcela | Vencimento | Valor** preenchida.
- 4 linhas: parcelas 1 a 4.
- **Vencimento:** datas mensais (dia 10 em cada mês, ou dia da 1ª parcela se não informar dia).
- **Valor:** parcelas 1–3 com mesmo valor; parcela 4 com ajuste de centavos para a soma dar exatamente o Saldo a pagar (R$ 80.000,00).

---

## 4. Persistência (rascunho)

- Clique em **Salvar rascunho** (ou avance e salve, conforme o fluxo do app).
- Volte para a **lista de contratos** e **abra de novo** o mesmo contrato.
- Vá até a etapa **Negócio**.

Resultado esperado:

- **Saldo a pagar** e os campos de parcelamento continuam preenchidos.
- A **tabela de parcelas** aparece com os mesmos valores e datas.

---

## 5. Desabilitar parcelamento (saldo volta a subir)

- Aumente de novo o saldo a pagar (ex.: zerar o valor do imóvel em permuta ou aumentar o valor total).
- Quando o **Saldo a pagar** ficar **maior ou igual** ao valor que estava antes da “redução”, o parcelamento deve **desabilitar** e a lista de parcelas deve **sumir** (e os campos Nº de parcelas e Data da 1ª parcela serem limpos).

---

## Resumo rápido

| Ação                         | Esperado                                      |
|-----------------------------|-----------------------------------------------|
| Reduzir Saldo a pagar       | Habilitar Nº parcelas, Data 1ª, tabela        |
| Preencher n + data + dia    | Tabela com Parcela \| Vencimento \| Valor     |
| Soma das parcelas           | Igual ao Saldo a pagar (R$)                   |
| Salvar e reabrir contrato   | Parcelas mantidas                             |
| Aumentar saldo de novo      | Parcelamento desabilitado, lista limpa        |

Se algo disso não acontecer, descreva a tela e o passo em que parou para ajustarmos o código.
