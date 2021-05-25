create table <table_name> (id INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY(Start with 1, Increment by 1),
                            date Date NOT NULL,
                            amount DECIMAL(10,5) NOT NULL,
                            summary varchar(512) NOT NULL,
                            -- Linked Entries
                            linkedentry_name varchar(255),
                            linkedentry_id INTEGER,
                            -- linked Loan Entries
                            linkedentry_capital DECIMAL(10,5),
                            linkedentry_interest DECIMAL(10,5),
                            linkedentry_insurance DECIMAL(10,5),
                            linkedentry_remaining DECIMAL(10,5)
                        )