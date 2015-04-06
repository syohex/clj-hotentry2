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

(defn- tag-value-fn [tag]
  (fn [x]
    (let [t (:tag x)]
      (when (= t tag)
        (first (:content x))))))

(defn- item->title [item]
  (some (tag-value-fn :title) item))

(defn- item->bookmarks [item]
  (some (tag-value-fn :hatena:bookmarkcount) item))

(defn- title-and-counts [items]
  (for [item items
        :let [content (:content item)
              title (item->title content)
              bookmarks (item->bookmarks content)]]
    (format "%s [%s]" title bookmarks)))

(defn- format-entries [entries]
  (map-indexed (fn [i e]
                 (format "%2d: %s"
                         (inc i) e))
               entries))

(defn -main [& args]
  (let [parsed (cli/parse-opts args options)
        opt (:options parsed)
        key (first (:arguments parsed))
        url (hotentry-url key (:threshold opt))
        parsed (parse-rss url)
        items (rest (:content parsed))
        entries (title-and-counts items)]
    (doseq [entry (take (:limit opt) (format-entries entries))]
      (println entry))))
