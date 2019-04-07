/*
* Copyright (c) 2019 Simer Plaha (@simerplaha)
*
* This file is a part of SwayDB.
*
* SwayDB is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* SwayDB is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with SwayDB. If not, see <https://www.gnu.org/licenses/>.
*/
package swaydb.memory;

import java.io.Closeable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import scala.Function1;
import scala.Option;
import scala.Tuple2;
import scala.collection.Iterable;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.Deadline;
import scala.concurrent.duration.FiniteDuration;
import swaydb.Prepare;
import swaydb.data.IO;
import swaydb.data.accelerate.Accelerator;
import swaydb.data.accelerate.Level0Meter;
import swaydb.data.api.grouping.KeyValueGroupingStrategy;
import swaydb.data.compaction.LevelMeter;
import swaydb.java.Serializer;

public class Map<K, V> implements Closeable {

    private final swaydb.Map<K, V, IO> database;

    private Map(swaydb.Map<K, V, IO> database) {
        this.database = database;
    }

    public int size() {
        return database.asScala().size();
    }

    public boolean isEmpty() {
        return (boolean) database.isEmpty().get();
    }

    public boolean nonEmpty() {
        return (boolean) database.nonEmpty().get();
    }

    public LocalDateTime expiration(K key) {
        Object result = database.expiration(key).get();
        if (result instanceof scala.Some) {
            Deadline expiration = (Deadline) ((scala.Some) result).get();
            return LocalDateTime.now().plusNanos(expiration.timeLeft().toNanos());
        }
        return null;
    }

    public Duration timeLeft(K key) {
        Object result = database.timeLeft(key).get();
        if (result instanceof scala.Some) {
            FiniteDuration duration = (FiniteDuration) ((scala.Some) result).get();
            return Duration.ofNanos(duration.toNanos());
        }
        return null;
    }

    public int keySize(K key) {
        return database.keySize(key);
    }

    public int valueSize(V value) {
        return database.valueSize(value);
    }

    public long sizeOfSegments() {
        return database.sizeOfSegments();
    }

    public Level0Meter level0Meter() {
        return database.level0Meter();
    }

    public Optional<LevelMeter> level1Meter() {
        return levelMeter(1);
    }

    public Optional<LevelMeter> levelMeter(int levelNumber) {
        Option<LevelMeter> levelMeter = database.levelMeter(levelNumber);
        return levelMeter.isEmpty() ? Optional.empty() : Optional.ofNullable(levelMeter.get());
    }

    public boolean containsKey(K key) {
        return (boolean) database.contains(key).get();
    }

    @SuppressWarnings("unchecked")
    public boolean mightContain(K key) {
        return (boolean) database.mightContain(key).get();
    }

    @SuppressWarnings("unchecked")
    public java.util.Map.Entry<K, V> head() {
        Object result = database.headOption().get();
        if (result instanceof scala.Some) {
            scala.Tuple2<K, V> tuple2 = (scala.Tuple2<K, V>) ((scala.Some) result).get();
            return new AbstractMap.SimpleEntry<>(tuple2._1(), tuple2._2());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Optional<java.util.Map.Entry<K, V>> headOption() {
        return Optional.ofNullable(head());
    }

    @SuppressWarnings("unchecked")
    public java.util.Map.Entry<K, V> last() {
        Object result = database.lastOption().get();
        if (result instanceof scala.Some) {
            scala.Tuple2<K, V> tuple2 = (scala.Tuple2<K, V>) ((scala.Some) result).get();
            return new AbstractMap.SimpleEntry<>(tuple2._1(), tuple2._2());
        }
        return null;
    }

    public Optional<java.util.Map.Entry<K, V>> lastOption() {
        return Optional.ofNullable(last());
    }

    public boolean containsValue(V value) {
        return values().contains(value);
    }

    public void putAll(java.util.Map<? extends K, ? extends V> map) {
        map.forEach((key, value) -> database.put(key, value));
    }

    public void updateAll(java.util.Map<? extends K, ? extends V> map) {
        map.forEach((key, value) -> database.update(key, value));
    }

    public void clear() {
        database.asScala().clear();
    }

    public Set<K> keySet() {
        Seq<Tuple2<K, V>> entries = database.asScala().toSeq();
        Set<K> result = new LinkedHashSet<>();
        for (int index = 0; index < entries.size(); index += 1) {
            Tuple2<K, V> tuple2 = entries.apply(index);
            result.add(tuple2._1());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public K keysHead() {
        Object result = database.keys().headOption().get();
        if (result instanceof scala.Some) {
            return (K) ((scala.Some) result).get();
        }
        return null;
    }

    public Optional<K> keysHeadOption() {
        return Optional.ofNullable(keysHead());
    }

    @SuppressWarnings("unchecked")
    public K keysLast() {
        Object result = database.keys().lastOption().get();
        if (result instanceof scala.Some) {
            return (K) ((scala.Some) result).get();
        }
        return null;
    }

    public Optional<K> keysLastOption() {
        return Optional.ofNullable(keysLast());
    }

    public Collection<V> values() {
        Seq<Tuple2<K, V>> entries = database.asScala().toSeq();
        Collection<V> result = new LinkedHashSet<>();
        for (int index = 0; index < entries.size(); index += 1) {
            Tuple2<K, V> tuple2 = entries.apply(index);
            result.add(tuple2._2());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        Seq<Tuple2<K, V>> entries = database.asScala().toSeq();
        Set<java.util.Map.Entry<K, V>> result = new LinkedHashSet<>();
        for (int index = 0; index < entries.size(); index += 1) {
            Tuple2<K, V> tuple2 = entries.apply(index);
            result.add(new AbstractMap.SimpleEntry<>(tuple2._1(), tuple2._2()));
        }
        return result;
    }

    public V put(K key, V value) {
        V oldValue = get(key);
        database.put(key, value).get();
        return oldValue;
    }

    public V put(K key, V value, long expireAfter, TimeUnit timeUnit) {
        V oldValue = get(key);
        database.put(key, value, FiniteDuration.create(expireAfter, timeUnit)).get();
        return oldValue;
    }

    public V put(K key, V value, LocalDateTime expireAt) {
        V oldValue = get(key);
        int expireAtNano = Duration.between(LocalDateTime.now(), expireAt).getNano();
        database.put(key, value, FiniteDuration.create(expireAtNano, TimeUnit.NANOSECONDS).fromNow()).get();
        return oldValue;
    }

    public V expire(K key, long after, TimeUnit timeUnit) {
        V oldValue = get(key);
        database.expire(key, FiniteDuration.create(after, timeUnit)).get();
        return oldValue;
    }

    public V expire(K key, LocalDateTime expireAt) {
        V oldValue = get(key);
        int expireAtNano = Duration.between(LocalDateTime.now(), expireAt).getNano();
        database.expire(key, FiniteDuration.create(expireAtNano, TimeUnit.NANOSECONDS).fromNow()).get();
        return oldValue;
    }

    public V update(K key, V value) {
        V oldValue = get(key);
        database.update(key, value).get();
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    public V get(K key) {
        Object result = database.get((K) key).get();
        if (result instanceof scala.Some) {
            return (V) ((scala.Some) result).get();
        }
        return null;
    }

    public V remove(K key) {
        V oldValue = get(key);
        database.remove(key).get();
        return oldValue;
    }

    public java.util.Map<K, V> asJava() {
        return JavaConverters.mapAsJavaMapConverter(database.asScala()).asJava();
    }

    @Override
    public void close() {
        database.closeDatabase().get();
    }

    @SuppressWarnings("unchecked")
    public Level0Meter commit(Prepare<K, V>... prepares) {
        List<Prepare<K, V>> preparesList = Arrays.asList(prepares);
        Iterable<Prepare<K, V>> prepareIterator
                = JavaConverters.iterableAsScalaIterableConverter(preparesList).asScala();
        return (Level0Meter) database.commit(prepareIterator).get();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> swaydb.memory.Map<K, V> create(Class<K> keySerializer, Class<V> valueSerializer) {
        int mapSize = Map$.MODULE$.apply$default$1();
        int segmentSize = Map$.MODULE$.apply$default$2();
        int cacheSize = Map$.MODULE$.apply$default$3();
        FiniteDuration cacheCheckDelay = Map$.MODULE$.apply$default$4();
        double bloomFilterFalsePositiveRate = Map$.MODULE$.apply$default$5();
        boolean compressDuplicateValues = Map$.MODULE$.apply$default$6();
        boolean deleteSegmentsEventually = Map$.MODULE$.apply$default$7();
        Option<KeyValueGroupingStrategy> groupingStrategy = Map$.MODULE$.apply$default$8();
        Function1<Level0Meter, Accelerator> acceleration = Map$.MODULE$.apply$default$9();

        swaydb.data.order.KeyOrder keyOrder = Map$.MODULE$.apply$default$12(mapSize, segmentSize,
                cacheSize, cacheCheckDelay, bloomFilterFalsePositiveRate, compressDuplicateValues,
                deleteSegmentsEventually, groupingStrategy, acceleration);
        ExecutionContext ec = Map$.MODULE$.apply$default$13(mapSize, segmentSize, cacheSize,
                cacheCheckDelay, bloomFilterFalsePositiveRate,
                compressDuplicateValues, deleteSegmentsEventually, groupingStrategy, acceleration);

        swaydb.memory.Map<K, V> memoryMap = new swaydb.memory.Map<>(
                (swaydb.Map<K, V, IO>) Map$.MODULE$.apply(mapSize, segmentSize, cacheSize,
                        cacheCheckDelay, bloomFilterFalsePositiveRate, compressDuplicateValues,
                        deleteSegmentsEventually, groupingStrategy, acceleration, Serializer.classToType(keySerializer),
                        Serializer.classToType(valueSerializer), keyOrder, ec).get());
        return memoryMap;
    }

    public static class Builder<K, V> {

        private int mapSize = Map$.MODULE$.apply$default$1();
        private int segmentSize = Map$.MODULE$.apply$default$2();
        private int cacheSize = Map$.MODULE$.apply$default$3();
        private FiniteDuration cacheCheckDelay = Map$.MODULE$.apply$default$4();
        private double bloomFilterFalsePositiveRate = Map$.MODULE$.apply$default$5();
        private boolean compressDuplicateValues = Map$.MODULE$.apply$default$6();
        private boolean deleteSegmentsEventually = Map$.MODULE$.apply$default$7();
        private Option<KeyValueGroupingStrategy> groupingStrategy = Map$.MODULE$.apply$default$8();
        private Function1<Level0Meter, Accelerator> acceleration = Map$.MODULE$.apply$default$9();
        private Class<?> keySerializer;
        private Class<?> valueSerializer;

        public Builder<K, V> withMapSize(int mapSize) {
            this.mapSize = mapSize;
            return this;
        }

        public Builder<K, V> withSegmentSize(int segmentSize) {
            this.segmentSize = segmentSize;
            return this;
        }

        public Builder<K, V> withCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder<K, V> withCacheCheckDelay(FiniteDuration cacheCheckDelay) {
            this.cacheCheckDelay = cacheCheckDelay;
            return this;
        }

        public Builder<K, V> withBloomFilterFalsePositiveRate(double bloomFilterFalsePositiveRate) {
            this.bloomFilterFalsePositiveRate = bloomFilterFalsePositiveRate;
            return this;
        }

        public Builder<K, V> withCompressDuplicateValues(boolean compressDuplicateValues) {
            this.compressDuplicateValues = compressDuplicateValues;
            return this;
        }

        public Builder<K, V> withDeleteSegmentsEventually(boolean deleteSegmentsEventually) {
            this.deleteSegmentsEventually = deleteSegmentsEventually;
            return this;
        }

        public Builder<K, V> withGroupingStrategy(Option<KeyValueGroupingStrategy> groupingStrategy) {
            this.groupingStrategy = groupingStrategy;
            return this;
        }

        public Builder<K, V> withAcceleration(Function1<Level0Meter, Accelerator> acceleration) {
            this.acceleration = acceleration;
            return this;
        }

        public Builder<K, V> withKeySerializer(Class<?> keySerializer) {
            this.keySerializer = keySerializer;
            return this;
        }

        public Builder<K, V> withValueSerializer(Class<?> valueSerializer) {
            this.valueSerializer = valueSerializer;
            return this;
        }

        @SuppressWarnings("unchecked")
        public swaydb.memory.Map<K, V> build() {
            swaydb.data.order.KeyOrder keyOrder = Map$.MODULE$.apply$default$12(mapSize, segmentSize,
                    cacheSize, cacheCheckDelay, bloomFilterFalsePositiveRate, compressDuplicateValues,
                    deleteSegmentsEventually, groupingStrategy, acceleration);
            ExecutionContext ec = Map$.MODULE$.apply$default$13(mapSize, segmentSize, cacheSize,
                    cacheCheckDelay, bloomFilterFalsePositiveRate,
                    compressDuplicateValues, deleteSegmentsEventually, groupingStrategy, acceleration);
            swaydb.memory.Map<K, V> memoryMap = new swaydb.memory.Map<>(
                    (swaydb.Map<K, V, IO>) Map$.MODULE$.apply(mapSize, segmentSize, cacheSize,
                            cacheCheckDelay, bloomFilterFalsePositiveRate, compressDuplicateValues,
                            deleteSegmentsEventually, groupingStrategy, acceleration,
                            Serializer.classToType(keySerializer), Serializer.classToType(valueSerializer),
                            keyOrder, ec).get());
            return memoryMap;
        }
    }

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

}
