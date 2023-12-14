(ns spmp-2.core
  (:require [clojure.core.async :as async]))

(defn matching-message [write-chan read-chans]
  (let [num-chans (count read-chans)
        result-chan (async/chan)
        messages (atom (vec (repeat num-chans nil)))
        count (atom 0)]
    (doseq [chan read-chans]
      (async/go
        (let [mess (async/<!! chan)]
          (swap! messages #(assoc %1 @count mess))
          (swap! count inc)
          (println "Message:" mess)
          (if (= @count num-chans)
            (do
              (reset! count 0)
              (let [common-mess (apply = @messages)]
                (when common-mess
                  (async/>! result-chan common-mess))))))))

    result-chan))

(defn -main []
  (let [write-chan (async/chan)
        read-chan1 (async/chan)
        read-chan2 (async/chan)]

    (async/go
      (async/>! read-chan1 "Тест"))

    (async/go
      (async/>! read-chan2 "Тест"))

    (let [result-chan (matching-message write-chan [read-chan1 read-chan2])]
      (println "Matches" (async/<!! result-chan)))))

(-main)
