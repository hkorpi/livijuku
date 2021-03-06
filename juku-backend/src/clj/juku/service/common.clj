(ns juku.service.common
  (:require [slingshot.slingshot :as ss]
            [juku.db.yesql-patch :as sql]
            [ring.util.http-response :as r]))

; *** Yleiset kyselyt ***
(sql/defqueries "common.sql")

(defn retry [tries f & args]
  (let [res (try {:value (apply f args)}
                 (catch Exception e
                   (if (= 0 tries)
                     (throw e)
                     {:exception e})))]
    (if (:exception res)
      (recur (dec tries) f args)
      (:value res))))
