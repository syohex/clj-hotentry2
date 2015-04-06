(ns hotentry2.core
  (:require [clojure.xml :as xml]
            [clojure.tools.cli :as cli]))

(def ^:private options
  [["-t" "--threshold NUM" "threshold of bookmarks"
    :default 3]
   ["-l" "--limit" "limit of printing entries"
    :default 10]
   ["-h" "--help"]])

(defn- parse-rss [url]
  (xml/parse url))

(defn- hotentry-url [keyword threshould]
  (format "http://b.hatena.ne.jp/search/tag?q=%s&users=%d&mode=rss"
          keyword threshould))

(defn- item->title [item]
  (some (fn [x]
          (when (contains? x :title)
            (:content x))) item))

(defn -main [& args]
  (let [parsed (cli/parse-opts args options)
        opt (:options parsed)
        key (first (:arguments parsed))
        url (hotentry-url key (:threshold opt))
        entries (parse-rss url)
        items (rest (:content entries))]
    (for [item items
          :let [content (:content item)]]
      (item->title item))))
