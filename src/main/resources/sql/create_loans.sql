create table <table_name> (name VARCHAR(255) PRIMARY KEY,
                           applicationFees  DECIMAL(10,5) Not null,
                           start Date Not null,
                           monthlyAmount DECIMAL(10,5) Not null,
                           rate DECIMAL(10,5) Not null,
                           amount DECIMAL(10,5) Not null,
                           insurance DECIMAL(10,5) Not null,
                           insurance_type VARCHAR(5) Not null
                        )