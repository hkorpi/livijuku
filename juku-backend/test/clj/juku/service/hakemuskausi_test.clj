(ns juku.service.hakemuskausi-test
  (:require [midje.sweet :refer :all]
            [juku.service.hakemuskausi :as hk]
            [juku.service.hakemus :as h]
            [common.collection :as c]
            [juku.service.test :as test]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [clj-http.fake :as fake])
  (:import (java.io ByteArrayInputStream)))

(def hakemuskausi (test/next-hakemuskausi!))
(def vuosi (:vuosi hakemuskausi))

(defn inputstream-from [txt] (ByteArrayInputStream. (.getBytes txt)))

(test/with-user "juku_kasittelija" ["juku_kasittelija"]
  (fake/with-fake-routes {
      #"http://(.+)/hakemuskausi" (fn [req] {:status 200 :headers {} :body "testing\n"})
      #"http://(.+)/hakemuskausi/(.+)/sulje" (fn [req] (println req) {:status 200 :headers {} :body ""})}

    (fact "Avaa hakemuskausi"
      (let [vuosi (:vuosi (test/next-hakemuskausi!))]
        (hk/save-hakuohje vuosi "test" "text/plain" (inputstream-from  "test"))
        (hk/avaa-hakemuskausi! vuosi)
        (:diaarinumero (hk/find-hakemuskausi {:vuosi vuosi})) => "testing"))

    (fact "Sulje hakemuskausi"
      (let [vuosi (:vuosi (test/next-hakemuskausi!))]
        (hk/save-hakuohje vuosi "test" "text/plain" (inputstream-from  "test"))
        (hk/avaa-hakemuskausi! vuosi)
        (hk/sulje-hakemuskausi! vuosi)))))

(fact "Uuden hakuohjeen tallentaminen ja hakeminen"
  (let [hakuohje {:vuosi vuosi :nimi "test" :contenttype "text/plain"}]

    (hk/save-hakuohje vuosi "test" "text/plain" (inputstream-from  "test"))
    (slurp (:sisalto (hk/find-hakuohje-sisalto vuosi))) => "test"))

(fact "Hakuohjeen hakeminen - tyhjä hakuohje"
  (let [hakemuskausi (test/next-hakemuskausi!)]
    (hk/find-hakuohje-sisalto (:vuosi hakemuskausi)) => nil))

(fact "Tallenna ja lataa määräräha"
  (let [maararaha {:vuosi vuosi :organisaatiolajitunnus "ELY" :maararaha 1M :ylijaama 1M}]

    (hk/save-maararaha! maararaha)
    (hk/find-maararaha vuosi "ELY") => (dissoc maararaha :vuosi :organisaatiolajitunnus)))

(fact "Hakemuskausiyhteenvetohaku"
  (let [hakemuskausi (test/next-hakemuskausi!)
        vuosi (:vuosi hakemuskausi)
        hakemus1 {:vuosi vuosi :hakemustyyppitunnus "AH0" :organisaatioid 1M}
        hakemus2 {:vuosi vuosi :hakemustyyppitunnus "AH0" :organisaatioid 2M}
        id1 (h/add-hakemus! hakemus1)]

      (h/add-hakemus! hakemus2)
      (h/laheta-hakemus! id1)

      (c/find-first (c/eq :vuosi vuosi) (hk/find-hakemuskaudet+summary)) =>
        {:vuosi      vuosi
         :tilatunnus "A"
         :hakuohje_contenttype nil
         :hakemukset #{{:hakemustyyppitunnus "AH0"
                       :hakemustilat #{{:hakemustilatunnus "K" :count 1M}, {:hakemustilatunnus "V" :count 1M}}
                       :hakuaika {:alkupvm (time/local-date (- vuosi 1) 9 1)
                                  :loppupvm (time/local-date (- vuosi 1) 12 15)}}

                       {:hakuaika {:alkupvm (time/local-date vuosi 7 1)
                                   :loppupvm (time/local-date vuosi 8 31)}, :hakemustilat #{}, :hakemustyyppitunnus "MH1"}

                       {:hakuaika {:alkupvm (time/local-date (+ vuosi 1) 1 1)
                                   :loppupvm (time/local-date (+ vuosi 1) 1 31)}, :hakemustilat #{}, :hakemustyyppitunnus "MH2"}}}))

