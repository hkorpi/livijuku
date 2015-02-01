
-- name: select-liitteet
select hakemusid, liitenumero, nimi, contenttype
from liite where hakemusid = :hakemusid and poistoaika is null

-- name: select-liite-sisalto
select contenttype, sisalto from liite where hakemusid = :hakemusid and liitenumero = :liitenumero

-- name: insert-liite!
insert into liite (hakemusid, liitenumero, nimi, contenttype, sisalto)
values (:hakemusid,
        (select nvl(max(p.liitenumero), 0) + 1 from liite p where p.hakemusid = :hakemusid),
        :nimi, :contenttype, :sisalto)

-- name: update-liite-set-poistoaika!
update liite set poistoaika = sysdate
where hakemusid = :hakemusid and liitenumero = :liitenumero