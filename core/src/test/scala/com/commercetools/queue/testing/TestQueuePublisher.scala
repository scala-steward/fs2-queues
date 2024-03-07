/*
 * Copyright 2024 Commercetools GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commercetools.queue.testing

import cats.effect.{IO, Resource}
import com.commercetools.queue.{QueuePublisher, QueuePusher}

class TestQueuePublisher[T](queue: TestQueue[T]) extends QueuePublisher[IO, T] {

  override def pusher: Resource[IO, QueuePusher[IO, T]] = Resource.pure(new TestQueuePusher(queue))

}
