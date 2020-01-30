(ns lein-resource-paths-relative.plugin
  (:require [org.satta.glob :as glob]
            [clojure.java.io :as io]))


(defn- relativize-path
  [path base-path]
  (let [base-path (str base-path "/")]
    (when (.startsWith path base-path)
      (.substring path (.length base-path)))))


(defn expand-path [base-path path]
  (->> (file-seq path)
       (keep
         (fn [file]
           (when (.isFile file)
             (when-let
               [path
                (relativize-path
                  (.getCanonicalPath file)
                  (.getCanonicalPath base-path))]
               {:type  :bytes
                :path  path
                :bytes (slurp file)}))))))


(defn middleware
  [project & args]
  (update project :filespecs concat
          (->> (:resource-paths-relative project)
               (mapcat (fn [[base-path path]]
                         (->> (glob/glob path)
                              (mapcat (partial expand-path (io/file base-path)))))))))


(comment
  (let [input [["src/" "src/*"]
               ["target/" "target/stale"]]]
    (->> input
         (mapcat (fn [[base-path path]]
                   (->> (glob/glob path)
                        (mapcat (partial expand-path (io/file base-path)))))))))

