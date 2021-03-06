(ns api-live-tests.app-utils
  (:require [api-live-tests.api :as api :refer [upload-and-create-app destroy-all-apps install-app]]
            [clojure.java.io :refer [file make-parents]]
            [clojure.java.shell :refer [sh]]
            [cheshire.core :refer [parse-string generate-string]]))

(defn mk-tmp-dir!
  "Creates a unique temporary directory on the filesystem. Typically in /tmp on
  *NIX systems. Returns a File object pointing to the new directory. Raises an
  exception if the directory couldn't be created after 10000 tries.

  https://gist.github.com/samaaron/1398198
  "
  []
  (let [base-dir (file (System/getProperty "java.io.tmpdir"))
        base-name (str (System/currentTimeMillis) "-" (long (rand 1000000000)) "-")
        tmp-base (str (if (.endsWith (.getPath base-dir) "/")
                        (.getPath base-dir)
                        (str (.getPath base-dir) "/"))
                      base-name)
        max-attempts 10000]
    (loop [num-attempts 1]
      (if (= num-attempts max-attempts)
        (throw (Exception. (str "Failed to create temporary directory after " max-attempts " attempts.")))
        (let [tmp-dir-name (str tmp-base num-attempts)
              tmp-dir (file tmp-dir-name)]
          (if (.mkdir tmp-dir)
            tmp-dir
            (recur (inc num-attempts))))))))

(defn hyphens-to-camel-case-name
  "e.g. hello-world -> helloWorld"
  [method-name]
  (clojure.string/replace method-name #"-(\w)"
                          #(clojure.string/upper-case (second %1))))

(defn keys-to-camel-case [data]
  (if (map? data)
    (into {}
          (for [[k v] data]
            [(hyphens-to-camel-case-name (name k)) (keys-to-camel-case v)]))
    data))

(defn serialize-app-to-tmpdir! [{:keys [translations manifest requirements app-js] :as app}]
  (let [dir (mk-tmp-dir!)
        translation-files (map #(file dir (str "translations/" % ".json")) translations)
        filenames-to-data {"manifest.json" (keys-to-camel-case manifest)
                           "requirements.json" requirements}]
    (doseq [[filename data] filenames-to-data]
      (spit (file dir filename)
            (generate-string data
                             {:pretty true})))
    (doseq [translation-file translation-files]
      (make-parents translation-file)
      (spit translation-file (generate-string {})))
    (when app-js (.createNewFile (file dir "app.js")))
    dir))

(defn zip [dir]
  (sh "zip" "-r" "app" "." :dir dir)
  (file dir "app.zip"))
