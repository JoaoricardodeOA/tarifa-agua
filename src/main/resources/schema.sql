CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_tabela_ativa
    ON tabela_tarifaria ((1))
    WHERE ativa = true;