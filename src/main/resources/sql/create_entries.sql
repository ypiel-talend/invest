create table <table_name> (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(Start with 1, Increment by 1),
                            date Date NOT NULL,
                            amount DECIMAL(15,6) NOT NULL,
                            incomes DECIMAL(15,6) NOT NULL,
                            outcomes DECIMAL(15,6) NOT NULL,
                            summary varchar(512) NOT NULL,
                            -- Linked Entries
                            linkedentry_name varchar(255),
                            linkedentry_id INTEGER,
                            -- linked Loan Entries
                            linkedentry_capital DECIMAL(15,6),
                            linkedentry_interest DECIMAL(15,6),
                            linkedentry_insurance DECIMAL(15,6),
                            linkedentry_remaining DECIMAL(15,6),
                            linkedentry_prepayment DECIMAL(15,6)
                        )