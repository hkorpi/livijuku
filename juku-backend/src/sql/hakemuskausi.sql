
-- name: update-hakemuskausi-set-hakuohje-sisalto!
update hakemuskausi set hakuohje_sisalto = :sisalto
where vuosi = :vuosi

-- name: merge-hakemuskausi-hakuohje!
merge into hakemuskausi
using (select :vuosi value from dual) vuosi
  on (hakemuskausi.vuosi = vuosi.value)
when matched then
  update set hakuohje_nimi = :nimi, hakuohje_contenttype = :contenttype
when not matched then
  insert (vuosi, hakuohje_nimi, hakuohje_contenttype)
  values (:vuosi, :nimi, :contenttype)

-- name: select-hakuohje-sisalto
select hakuohje_contenttype contenttype, hakuohje_sisalto sisalto from hakemuskausi where vuosi = :vuosi