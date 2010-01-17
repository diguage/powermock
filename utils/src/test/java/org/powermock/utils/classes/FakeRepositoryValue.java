/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.powermock.utils.classes;

import static org.powermock.utils.model.synchronizer.SynchronizerOperationOptions.duration;

import java.util.concurrent.TimeUnit;

import org.powermock.utils.model.synchronizer.Condition;
import org.powermock.utils.model.synchronizer.Duration;

public class FakeRepositoryValue implements Condition<Integer> {
	private final FakeRepository repository;

	public FakeRepositoryValue(FakeRepository repository) {
		this.repository = repository;
	}

	public Integer condition() {
		return repository.getValue();
	}

	public Duration getPollInterval() {
		return duration(200, TimeUnit.MILLISECONDS);
	}
}
