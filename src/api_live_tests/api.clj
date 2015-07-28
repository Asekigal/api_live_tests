(ns api-live-tests.api
  (:require [clj-http.client :as client]))

(def token (System/getenv "API_TOKEN"))
(def api-url (System/getenv "API_URL"))
(defn apps-url [path] (str api-url "/apps" path))
(def auth-creds [(str (System/getenv "API_EMAIL") "/token") token])

(println "env vars!")
(println api-url)

(defn create-upload [app-zip-filename]
  (let [response (client/post (apps-url "/uploads.json")
                              {:basic-auth auth-creds
                               :as :json
                               :multipart [{:name "uploaded_data"
                                            :content (clojure.java.io/file app-zip-filename)}]})]
    (get-in response [:body :id])))

(defn create-app [upload-id app-name]
  (let [response (client/post (str api-url "/apps.json")
                              {:basic-auth auth-creds
                               :form-params {:name app-name
                                             :short_description "a description"
                                             :upload_id upload-id}
                               :content-type :json
                               :as :json})]
    (get-in response [:body :job_id])))

(defn get-job-status [job-id]
  (let [response (client/get (apps-url (str "/job_statuses/" job-id ".json"))
                             {:basic-auth auth-creds
                              :as :json})]
    (:body response)))

(defn get-installation-job-status [job-id]
  (let [response (client/get (apps-url (str "/installations/job_statuses/" job-id ".json"))
                             {:basic-auth auth-creds
                              :as :json})]
    (:body response)))

(defn app-id-when-job-completed [job-id]
  (loop [job-status (get-job-status job-id)]
    (case (:status job-status)
      "completed" (:app_id job-status)
      "failed" (do
                 (println "FAILURE FAILURE FAILURE")
                 (throw (str "Job failed: " (:message job-status))))
      (recur (get-job-status job-id)))))

(defn upload-and-create-app [app-zip-filename app-name]
  (let [upload-id (create-upload app-zip-filename)
        job-status-id (create-app upload-id app-name)]
    (app-id-when-job-completed job-status-id)))

(defn installation-id-when-job-completed [job-id]
  (loop [job-status (get-installation-job-status job-id)]
    (case (:status job-status)
      "completed" (:installation_id job-status)
      "failed" (do
                 (println "FAILURE FAILURE FAILURE")
                 job-status)
      (recur (get-installation-job-status job-id)))))

(defn start-app-install-map [http-options]
  (let [response (client/post (apps-url "/installations.json")
                              (merge
                                {:basic-auth auth-creds
                                 :as :json}
                                http-options))]
    (println response)
    (-> response :body :pending_job_id)))

(defn start-app-install [app-id installation-name]
  (let [response (client/post (apps-url "/installations.json")
                              {:basic-auth auth-creds
                               :form-params {:settings {:name installation-name}
                                             :app_id app-id}
                               :content-type :json
                               :as :json})]
    (println response)
    (-> response :body :pending_job_id)))

(defn install-non-reqs-app [app-id installation-name]
  (let [response (client/post (apps-url "/installations.json")
                              {:basic-auth auth-creds
                               :form-params {:settings {:name installation-name}
                                             :app_id app-id}
                               :content-type :json
                               :as :json})]
    (println response)
    (-> response :body :id)))

(defn install-app [app-id installation-name]
  (let [job-id (start-app-install app-id installation-name)]
    (installation-id-when-job-completed job-id)))


(defn uninstall-app [installation-id]
  (client/delete (apps-url (str "/installations/" installation-id ".json"))
                 {:basic-auth auth-creds
                  :content-type :json
                  :as :json}))


(defn delete-app [app-id]
  (client/delete (apps-url (str "/" app-id ".json"))
                 {:basic-auth auth-creds
                  :content-type :json
                  :as :json}))


(defn get-owned-apps []
  (let [response (client/get (apps-url (str "/owned.json"))
                             {:basic-auth auth-creds
                              :as :json})]
    (get-in response [:body :apps])))


(defn destroy-all-apps []
  (doseq [app-id (map :id (get-owned-apps))]
    (delete-app app-id)))
