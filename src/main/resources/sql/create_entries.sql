create table <table_name> (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(Start with 1, Increment by 1),
                            date Date NOT NULL,
                            amount DECIMAL(11,6) NOT NULL,
                            incomes DECIMAL(11,6) NOT NULL,
                            outcomes DECIMAL(11,6) NOT NULL,
                            summary varchar(512) NOT NULL,
                            -- Linked Entries
                            linkedentry_name varchar(255),
                            linkedentry_id INTEGER,
                            -- linked Loan Entries
                            linkedentry_capital DECIMAL(11,6),
                            linkedentry_interest DECIMAL(11,6),
                            linkedentry_insurance DECIMAL(11,6),
                            linkedentry_remaining DECIMAL(11,6),
                            linkedentry_prepayment DECIMAL(11,6)
                        )