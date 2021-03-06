(defproject api-live-tests "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main api-live-tests.core
  :injections [(require 'spyscope.core)]
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [prismatic/plumbing "0.4.4"]
                 [w01fe/sniper "0.1.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [clj-http "2.0.0"]
                 [clj-statsd "0.3.11"]
                 [org.clojure/test.check "0.7.0"]
                 [com.velisco/herbert "0.6.11"]
                 [com.gfredericks/test.chuck "0.1.21"]
                 [spyscope "0.1.5"]
                 [org.clojure/tools.trace "0.7.8"]
                 [backtick "0.3.3"]
                 [debugger "0.1.7"]
                 [clj-zendesk "0.3.0-SNAPSHOT"]
                 [clj-time "0.10.0"]])
