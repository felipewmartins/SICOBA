CREATE TABLE observacao_cliente (
  id                		SERIAL    NOT NULL PRIMARY KEY,
  codigo_cliente    		INT  DEFAULT NULL REFERENCES cliente (id),
  observacao 				CHARACTER VARYING(255) NOT NULL,
  data_inclusao     		TIMESTAMP NOT NULL,
  created_at                TIMESTAMP NOT NULL,
  updated_at                TIMESTAMP NOT NULL
);