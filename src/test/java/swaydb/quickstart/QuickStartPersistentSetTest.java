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
package swaydb.quickstart;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;
import swaydb.base.TestBase;
import swaydb.data.config.MMAP;
import swaydb.data.config.RecoveryMode;

@SuppressWarnings({"checkstyle:JavadocMethod", "checkstyle:JavadocType"})
public class QuickStartPersistentSetTest extends TestBase {

    @BeforeClass
    public static void beforeClass() throws IOException {
        deleteDirectoryWalkTreeStartsWith("target/disk3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void persistentSetInt() {
        // Create a persistent set database. If the directories do not exist, they will be created.
        // val db = persistent.Set[Int](dir = dir.resolve("disk3")).get

        final swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set.create(
                Integer.class,
                addTarget(Paths.get("disk3")));
        // db.add(1).get
        db.add(1);
        // db.get(1).get
        boolean result = db.contains(1);
        assertThat("result contains value", result, equalTo(true));
        // db.remove(1).get
        db.remove(1);
        boolean result2 = db.contains(1);
        assertThat("Empty result", result2, equalTo(false));

        db.commit(
                new swaydb.java.Prepare<Integer, scala.runtime.Nothing$>().put(2, null),
                new swaydb.java.Prepare().remove(1)
        );

        assertThat("two value", db.contains(2), equalTo(true));
        assertThat(db.contains(1), equalTo(false));
    }

    @Test
    public void persistentSetIntIterator() {
        final swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                .<Integer>builder()
                .withDir(addTarget(Paths.get("disk3iterator")))
                .withKeySerializer(Integer.class)
                .build();
        db.add(1);
        assertThat(db.iterator().next(), equalTo(1));
    }

    @Test
    public void persistentSetIntToArray() {
        final swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                .<Integer>builder()
                .withDir(addTarget(Paths.get("disk3toarray")))
                .withKeySerializer(Integer.class)
                .build();
        db.add(1);
        assertThat(Arrays.toString(db.toArray()), equalTo("[1]"));
        assertThat(Arrays.toString(db.toArray(new Integer[]{})), equalTo("[1]"));
    }

    @Test
    public void persistentSetIntAddExpireAfter() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3addexpireafter")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1, 100, TimeUnit.MILLISECONDS);
            assertThat(db.contains(1), equalTo(true));
            await().atMost(1800, TimeUnit.MILLISECONDS).until(() -> {
                assertThat(db.contains(1), equalTo(false));
                return true;
            });
        }
    }

    @Test
    public void persistentSetIntAddExpireAt() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3addexpireat")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1, LocalDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(100)));
            assertThat(db.contains(1), equalTo(true));
            await().atMost(1000, TimeUnit.MILLISECONDS).until(() -> {
                assertThat(db.contains(1), equalTo(false));
                return true;
            });
        }
    }

    @Test
    public void persistentSetIntExpireAfter() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3expireafter")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            db.expire(1, 100, TimeUnit.MILLISECONDS);
            assertThat(db.contains(1), equalTo(true));
            await().atMost(1800, TimeUnit.MILLISECONDS).until(() -> {
                assertThat(db.contains(1), equalTo(false));
                return true;
            });
        }
    }

    @Test
    public void persistentSetIntExpireAt() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3expireat")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            db.expire(1, LocalDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(100)));
            assertThat(db.contains(1), equalTo(true));
            await().atMost(1800, TimeUnit.MILLISECONDS).until(() -> {
                assertThat(db.contains(1), equalTo(false));
                return true;
            });
        }
    }

    @Test
    public void persistentSetIntContainsAll() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3containsall")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            assertThat(db.containsAll(Arrays.asList(1)), equalTo(true));
        }
    }

    @Test
    public void persistentSetIntAddAll() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3addall")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            db.add(Arrays.asList(2));
            assertThat(db.containsAll(Arrays.asList(1, 2)), equalTo(true));
        }
    }

    @Test
    public void persistentSetIntRetainAll() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3retainall")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            db.add(Arrays.asList(2));
            db.retainAll(Arrays.asList(1));
            assertThat(db.containsAll(Arrays.asList(1)), equalTo(true));
            db.retainAll(Arrays.asList(3));
            assertThat(db.containsAll(Arrays.asList(1)), equalTo(false));
        }
    }

    @Test
    public void memorySetIntRetainAll2() {
        try (swaydb.java.persistent.Set<String> boxes = swaydb.java.persistent.Set
                        .<String>builder()
                        .withDir(addTarget(Paths.get("disk3retainall2")))
                        .withKeySerializer(String.class)
                        .build()) {
            List<String> bags = new ArrayList<>();
            bags.add("pen");
            bags.add("pencil");
            bags.add("paper");

            boxes.add("pen");
            boxes.add("paper");
            boxes.add("books");
            boxes.add("rubber");

            boxes.retainAll(bags);
            assertThat(Arrays.toString(boxes.toArray()), equalTo("[paper, pen]"));
        }
    }

    @Test
    public void persistentSetIntRemove() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3removeall")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            db.add(2);
            db.remove(new HashSet<>(Arrays.asList(1, 2)));
            assertThat(Arrays.toString(db.toArray()), equalTo("[]"));
            db.add(3);
            db.add(4);
            db.remove(3, 4);
            assertThat(Arrays.toString(db.toArray()), equalTo("[]"));
        }
    }

    @Test
    public void persistentSetIntSize() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3size")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            db.add(Arrays.asList(2));
            assertThat(db.size(), equalTo(2));
            db.remove(1);
            assertThat(db.size(), equalTo(1));
        }
    }

    @Test
    public void persistentSetIntIsEmpty() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3isempty")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            assertThat(db.isEmpty(), equalTo(true));
            db.add(1);
            assertThat(db.isEmpty(), equalTo(false));
        }
    }

    @Test
    public void persistentSetIntNonEmpty() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3nonempty")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            assertThat(db.nonEmpty(), equalTo(false));
            db.add(1);
            assertThat(db.nonEmpty(), equalTo(true));
        }
    }

    @Test
    public void persistentSetIntExpiration() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3expiration")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            LocalDateTime expireAt = LocalDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(100));
            db.add(1, expireAt);
            assertThat(db.expiration(1).truncatedTo(ChronoUnit.SECONDS).toString(),
                    equalTo(expireAt.truncatedTo(ChronoUnit.SECONDS).toString()));
            assertThat(db.expiration(2), nullValue());
        }
    }

    @Test
    public void persistentSetIntTimeLeft() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3timeleft")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            LocalDateTime expireAt = LocalDateTime.now().plusNanos(TimeUnit.MILLISECONDS.toNanos(100));
            db.add(1, expireAt);
            assertThat(db.timeLeft(1).getSeconds(), equalTo(0L));
            assertThat(db.timeLeft(2), nullValue());
        }
    }

    @Test
    public void persistentSetIntSizes() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3sizes")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            assertThat(db.sizeOfSegments(), equalTo(0L));
            assertThat(db.level0Meter().currentMapSize(), equalTo(4000000L));
            assertThat(db.level1Meter().get().levelSize(), equalTo(0L));
            assertThat(db.levelMeter(1).get().levelSize(), equalTo(0L));
            assertThat(db.levelMeter(8).isPresent(), equalTo(false));
        }
    }

    @Test
    public void persistentSetIntMightContain() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3mightContain")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            assertThat(db.mightContain(1), equalTo(true));
        }
    }

    @Test
    public void persistentSetIntAsJava() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3asjava")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            assertThat(db.asJava().size(), equalTo(1));
        }
    }

    @Test
    public void persistentSetIntClear() {
        try (swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                        .<Integer>builder()
                        .withDir(addTarget(Paths.get("disk3clear")))
                        .withKeySerializer(Integer.class)
                        .build()) {
            db.add(1);
            assertThat(db.isEmpty(), equalTo(false));
            db.clear();
            assertThat(db.isEmpty(), equalTo(true));
        }
    }

    @Test
    public void persistentSetIntFromBuilder() {
        // val db = persistent.Set[Int](dir = dir.resolve("disk3builder")).get
        final swaydb.java.persistent.Set<Integer> db = swaydb.java.persistent.Set
                .<Integer>builder()
                .withDir(addTarget(Paths.get("disk3builder")))
                .withKeySerializer(Integer.class)
                .withMaxOpenSegments(1000)
                .withMemoryCacheSize(100000000)
                .withMapSize(4000000)
                .withMmapMaps(true)
                .withRecoveryMode(RecoveryMode.ReportFailure$.MODULE$)
                .withMmapAppendix(true)
                .withMmapSegments(MMAP.WriteAndRead$.MODULE$)
                .withSegmentSize(2000000)
                .withAppendixFlushCheckpointSize(2000000)
                .withOtherDirs(swaydb.persistent.Set$.MODULE$.apply$default$11())
                .withMemorySweeperPollInterval(scala.concurrent.duration.FiniteDuration.apply(5, TimeUnit.SECONDS))
                .withCompressDuplicateValues(true)
                .withDeleteSegmentsEventually(false)
                .withLastLevelGroupBy(scala.Option.empty())
                .withAcceleration(swaydb.persistent.Map$.MODULE$.apply$default$19())
                .build();
        // db.add(1).get
        db.add(1);
        // db.contains(1).get
        boolean result = db.contains(1);
        assertThat("result contains value", result, equalTo(true));
        // db.remove(1).get
        db.remove(1);
        boolean result2 = db.contains(1);
        assertThat("Empty result", result2, equalTo(false));
    }
}
