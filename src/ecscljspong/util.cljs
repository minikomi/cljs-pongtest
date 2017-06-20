(ns ecscljspong.util)

(defn vec->str-arr [^:not-native coll]
  (let [arr (array)]
    (doseq [v coll]
      (.push arr (name v)))
    arr))

(defn get-component [entity c-name]
  (.get entity (name c-name)))

(defn add-component [entity c-name c-data]
  (.addComponent entity (name c-name) c-data))

(defn query-components [em cs]
  (.queryComponents em (vec->str-arr cs)))

(defn get-global [em kw]
  (get (.-globals em) kw))

(defn clamp [v l h]
  (min h (max l v)))

(defn get-width [em]
  (.-width (.-view (get-global em :renderer))))

(defn get-height [em]
  (.-height (.-view (get-global em :renderer))))
