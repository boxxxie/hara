(ns hara.core.base.event.handler-test
  (:use hara.test)
  (:require [hara.core.base.event.handler :refer :all]))

^{:refer hara.core.base.event.handler/new-id :added "3.0"}
(fact "creates a random id with a keyword base"
  (new-id)
  ;;=> :06679506-1f87-4be8-8cfb-c48f8579bc00
)

^{:refer hara.core.base.event.handler/expand-data :added "3.0"}
(fact "expands shorthand data into a map"

  (expand-data :hello)
  => {:hello true}

  (expand-data [:hello {:world "foo"}])
  => {:world "foo", :hello true})

^{:refer hara.core.base.event.handler/check-data :added "3.0"}
(fact "checks to see if the data corresponds to a template"

  (check-data {:hello true} :hello)
  => true

  (check-data {:hello true} {:hello true?})
  => true

  (check-data {:hello true} '_)
  => true

  (check-data {:hello true} #{:hello})
  => true)

^{:refer hara.core.base.event.handler/manager :added "3.0"}
(fact "creates a new manager"
  (manager)
  ;; => #hara.core.base.event.handler.Manager{:id :b56eb2c9-8d21-4680-b3e1-0023ae685d2b,
  ;;                               :store [], :options {}}
)

^{:refer hara.core.base.event.handler/remove-handler :added "3.0"}
(fact "adds a handler to the manager"
  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (remove-handler :hello)
      (match-handlers {:hello "world"}))
  => ())

^{:refer hara.core.base.event.handler/add-handler :added "3.0"}
(fact "adds a handler to the manager"
  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (match-handlers {:hello "world"})
      (count))
  => 1)

^{:refer hara.core.base.event.handler/list-handlers :added "3.0"}
(fact "list handlers that are present for a given manager"

  (list-handlers (manager))
  => [])

^{:refer hara.core.base.event.handler/match-handlers :added "3.0"}
(fact "match handlers for a given manager"

  (-> (add-handler (manager) :hello {:id :hello
                                     :handler identity})
      (match-handlers {:hello "world"}))
  => (contains-in [{:id :hello
                    :handler fn?
                    :checker :hello}]))

(comment
  (hara.code/import))
