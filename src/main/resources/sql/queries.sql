-- cumul all loan interest byt year/month
select YEAR(date) as y, MONTH(date) as m, sum(LINKEDENTRY_INTEREST) as interests
    from invest_b5a0e08_entities
    group by  YEAR(date), MONTH(date)
    order by  YEAR(date), MONTH(date);