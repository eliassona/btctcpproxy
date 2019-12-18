(ns btctcpproxy.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]))

(defmacro dbg [body]
  `(let [x# ~body]
     (println "dbg:" '~body "=" x#)
     x#))

(def ^:private id (atom 0))

(defn sys-prop-of [name default-value]
  (System/getProperty name default-value))

(def config 
  (atom {:user (sys-prop-of "bitcoinrpc.user" "bixus"), 
         :password (sys-prop-of "bitcoinrpc.password" "masoigjo2i4jo28uijksnto82uw5ojsbkjw395iow,gdlfklsitupqjglsk"), 
         :url (sys-prop-of "bitcoinrpc.url" "http://localhost:8392")}))

(defn btc-rpc-fn 
  ([json-map config]
      (let [{:keys [user password url]} config]
        (-> (client/post url 
                         {:body (dbg (json/json-str (assoc json-map :id  (str "id" (swap! id inc)))))
                          :headers {"Content-Type" "application/json; charset=utf-8"}
                          :basic-auth [user password]
                          :throw-entire-message? true}) :body identity)))
  ([json-text]
    (btc-rpc-fn json-text @config)))



(defmulti decode-cmd (fn [json-map] (json-map "cmd")))

(defn parse-cmd [cmd]
  (dbg cmd)
  (decode-cmd (if (string? cmd) (json/read-str cmd) cmd)))


(defmethod decode-cmd "rpc" [cmd] (btc-rpc-fn (dbg (cmd "payload")) @config))


