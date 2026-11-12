package br.com.entidade;

public enum SituacaoPedido {
    EM_PREPARO {
        @Override
        public boolean permiteAlteracao() {
            return true;
        }

        @Override
        public boolean bloqueiaFechamentoConta() {
            return true;
        }

        @Override
        public boolean entraNoTotal() {
            return false;
        }
    },

    FINALIZADO {
        @Override
        public boolean permiteAlteracao() {
            return false;
        }

        @Override
        public boolean bloqueiaFechamentoConta() {
            return false;
        }

        @Override
        public boolean entraNoTotal() {
            return true;
        }
    },

    CANCELADO {
        @Override
        public boolean permiteAlteracao() {
            return false;
        }

        @Override
        public boolean bloqueiaFechamentoConta() {
            return false;
        }

        @Override
        public boolean entraNoTotal() {
            return false;
        }
    };

    public abstract boolean permiteAlteracao();

    public abstract boolean bloqueiaFechamentoConta();

    public abstract boolean entraNoTotal();
}
