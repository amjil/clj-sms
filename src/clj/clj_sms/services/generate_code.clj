(ns clj-sms.services.generate-code)

(defn generate []
  (apply str (take 6 (repeatedly #(rand-int 10)))))
