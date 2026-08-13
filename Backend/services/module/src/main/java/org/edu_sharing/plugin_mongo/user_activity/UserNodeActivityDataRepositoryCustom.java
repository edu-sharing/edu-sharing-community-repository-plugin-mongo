package org.edu_sharing.plugin_mongo.user_activity;

public interface UserNodeActivityDataRepositoryCustom {

    /**
     * Persists the activity data with a MongoDB server-assigned timestamp (via {@code $currentDate})
     * instead of trusting {@code entity.getTimestamp()}.
     *
     * <p>This is intentionally a distinctly-named method rather than an override of
     * {@code MongoRepository.save(...)}: declaring a fragment method with the exact same erased
     * signature as {@code CrudRepository.save(S)} compiles at the Spring Data proxy level, but
     * javac itself then rejects any call to {@code repository.save(...)} as an ambiguous method
     * reference/invocation between the two identical-signature declarations - this is a hard
     * language-level conflict, not just a style choice, so callers must use this method by name.
     * {@link UserNodeActivityTracker} is currently this repository's only writer; if that ever
     * changes, any new writer must call this method too, not the inherited {@code save(...)}.
     *
     * <p>{@link UserNodeActivityTracker} runs on an {@code @Async} thread with no Alfresco
     * transaction bound, so its write happens immediately with no "commit" boundary to anchor a
     * client-captured timestamp to. Under concurrent writes, a client-captured {@code new Date()}
     * can end up stored out of true write order (e.g. thread scheduling delays, GC pauses between
     * capturing the timestamp and the write actually reaching MongoDB) - a poller that advances its
     * cursor based on such a timestamp could then permanently skip a slightly-delayed write once it
     * finally lands with an earlier timestamp than one already processed. Letting MongoDB assign the
     * timestamp at the moment the write is actually applied on the primary eliminates that gap
     * entirely: two writes' timestamps then always reflect their true application order, with no
     * window in which an earlier-timestamped write could still be pending after a later one is
     * already visible. This assumes reads are served from the primary (the driver's default, and
     * this module does not override it) - if read preference is ever changed to allow secondary
     * reads, primary/secondary replication lag would reintroduce a similar (much smaller) gap.
     *
     * <p>TODO: as of now, no read preference override exists anywhere for this repository/module,
     * so reads default to the driver's primary() - confirmed intentional, not just unconfigured.
     * If that ever changes (e.g. secondaryPreferred for read scaling), this safety guarantee breaks
     * silently. Must keep polling reads pinned to the primary, or add a replica-lag-aware watermark
     * (mirroring the pg_stat_activity based one in ShareInfoOpLogMapper on the tracker's Postgres
     * side) if that topology changes.
     */
    UserNodeActivityData saveWithServerTimestamp(UserNodeActivityData entity);
}
