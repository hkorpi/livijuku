(ns juku.schema.hakemus
  (:import (org.joda.time DateTime)
           (org.joda.time LocalDate))
  (:require [schema.core :as s]
            [clj-time.core :as time]))

(s/defschema Hakuaika {:alkupvm LocalDate
                       :loppupvm LocalDate})

(s/defschema Hakemus {:id     s/Num
                      :diaarinumero     s/Num
                      :vuosi  s/Int
                      :hakemustyyppitunnus s/Str
                      :hakemustilatunnus s/Str
                      :organisaatioid s/Num
                      :hakuaika Hakuaika
                      :muokkausaika DateTime})

(s/defschema HakemusSuunnitelma
     (assoc Hakemus :haettu-avustus s/Num
                    :myonnettava-avustus s/Num))

(s/defschema Hakemus+ (assoc Hakemus :selite (s/maybe s/Str)))

(s/defschema New-Hakemus (dissoc Hakemus :id :hakemustilatunnus :muokkausaika))

(s/defschema Hakemukset [Hakemus])

(s/defschema Hakemuskausi {:vuosi      s/Int
                           :hakemukset [Hakemus]})

(s/defschema Hakemuskausi+ {:vuosi      s/Int
                            :tilatunnus s/Str
                            :hakuohje_contenttype s/Str
                            :hakemukset #{Hakemus}})

(s/defschema Hakemuskaudet [Hakemuskausi])

(s/defschema Avustuskohde {:hakemusid     s/Num
                           :avustuskohdelajitunnus s/Str
                           :haettavaavustus s/Num,
                           :omarahoitus s/Num})


