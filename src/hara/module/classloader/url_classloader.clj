(ns hara.module.classloader.url-classloader
  (:require [hara.string :as string]
            [hara.data.base.seq :as seq]
            [hara.module.classloader.common :as common]
            [hara.protocol.loader :as protocol.loader]
            [hara.object.query :as reflect])
  (:import (java.net URL URLClassLoader)))

(defonce loader-access-ucp  (reflect/query-class URLClassLoader ["ucp" :#]))

(defonce +ucp-type+ (:type loader-access-ucp))

(defonce ucp-get-urls (reflect/query-class +ucp-type+ ["getURLs" :#]))

(defonce ucp-access-lmap (reflect/query-class +ucp-type+ ["lmap" :#]))

(defonce ucp-access-urls (reflect/query-class +ucp-type+ ["urls" :#]))

(defonce ucp-access-path (reflect/query-class +ucp-type+ ["path" :#]))

(defonce ucp-access-loaders (reflect/query-class +ucp-type+ ["loaders" :#]))

(defonce ucp-get-urls (reflect/query-class +ucp-type+ ["getURLs" :#]))

(defonce ucp-add-url  (reflect/query-class +ucp-type+ ["addURL" :#]))

(defn- ucp-remove-url
  [ucp ^URL entry]
  (let [paths (ucp-access-path ucp)      ;; util.ArrayList
        urls (ucp-access-urls ucp)       ;; util.Stack
        loaders (ucp-access-loaders ucp) ;; util.ArrayList 
        lmap (ucp-access-lmap ucp)       ;; util.HashMap

        ;;find entry in paths
        paths-entry (seq/element-of #(= (str %) (str entry)) paths)
        _  (if paths-entry (.remove paths paths-entry))

        ;;find entry in stack and delete it
        urls-entry (seq/element-of #(= (str %) (str entry)) urls)
        _ (if urls-entry (.remove urls urls-entry))

        ;; find loader in lookup
        url-key (seq/element-of #(= %
                                    (str (.getProtocol entry)
                                         "://"
                                         (.getFile entry)))
                                (keys lmap))
        loader-entry (.get lmap url-key)

        ;; remove entries from loader and stack 
        _ (if url-key (.remove lmap url-key))
        _ (if loader-entry (.remove loaders loader-entry))]
    [paths-entry urls-entry url-key loader-entry]))

(defmethod print-method URLClassLoader
  [^URLClassLoader v ^java.io.Writer w]
  (.write w (str "#loader@"
                 (.hashCode v)
                 (->> (protocol.loader/-all-urls v)
                      (mapv #(-> (str %)
                                 (string/split #"/")
                                 last))))))

(extend-type (identity +ucp-type+) ;; jdk.internal.loader.URLClassPath
  protocol.loader/ILoader
  (-has-url?    [ucp path]
    (boolean (protocol.loader/-get-url ucp path)))
  (-get-url     [ucp path]
    (seq/element-of #(= (str %) (str (common/to-url path)))
                    (ucp-access-path ucp)))
  (-all-urls    [ucp]
    (seq (ucp-get-urls ucp)))
  (-add-url     [ucp path]
    (if (not (protocol.loader/-has-url? ucp path))
      (ucp-add-url ucp  (common/to-url path))))
  (-remove-url  [ucp path]
    (if (protocol.loader/-has-url? ucp path)
      (ucp-remove-url ucp (common/to-url path)))))

(extend-type URLClassLoader
  protocol.loader/ILoader
  (-has-url?    [loader path]
    (protocol.loader/-has-url?   (loader-access-ucp loader) path))
  (-get-url     [loader path]
    (protocol.loader/-get-url    (loader-access-ucp loader) path))
  (-all-urls    [loader]
    (protocol.loader/-all-urls   (loader-access-ucp loader)))
  (-add-url     [loader path]
    (protocol.loader/-add-url    (loader-access-ucp loader) path))
  (-remove-url  [loader path]
    (protocol.loader/-remove-url (loader-access-ucp loader) path)))
