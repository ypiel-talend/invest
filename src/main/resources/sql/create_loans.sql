create table <table_name> (name VARCHAR(255) PRIMARY KEY,
                           applicationFees  DECIMAL(11,6) Not null,
                           start Date Not null,
                           monthlyAmount DECIMAL(11,6) Not null,
                           rate DECIMAL(11,6) Not null,
                           amount DECIMAL(11,6) Not null,
                           insurance DECIMAL(11,6) Not null,
                           insurance_type VARCHAR(5) Not null
                        )