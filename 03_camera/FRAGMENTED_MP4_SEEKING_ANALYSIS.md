# Fragmented MP4 (fMP4) Seeking Analysis

Status: investigation only. No production code was changed.
Scope: `03_camera` fMP4 recording path, and `04_portal` video playback path.
Date of research: 2026-10-19.
Media3 version in both apps: `1.11.1` (`03_camera/gradle/libs.versions.toml:20`, `04_portal/gradle/libs.versions.toml:2`). This is the latest Media3 release on Google Maven as of the research date.

---

## 1. Summary

**Root cause (high confidence, from source code of the exact Media3 version used):**
Files written by `FragmentedMedia3Muxer` (Media3 `FragmentedMp4Muxer` 1.11.1) contain no random-access index: no `sidx`, no `mfra`/`tfra`/`mfro`. When Media3's `FragmentedMp4Extractor` finds no index, it publishes `SeekMap.Unseekable`. Then:

1. ExoPlayer marks the media item as not seekable, so `COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM` is not available. Seek bars that check this command refuse the drag.
2. Any seek that still reaches `ProgressiveMediaPeriod` is changed to position 0 ("Treat all seeks into non-seekable media as being to t=0").

So a seek of any size, including 2–4 seconds, either does nothing or jumps back to the start. The 2–4 second size is not special. This is not a buffering problem, and it is not a problem with the Portal's seek increments.

**Why Portal's existing workaround does not help:** Portal turns on `FLAG_READ_MFRA_FOR_SEEK_MAP`, but the files do not have an `mfra` box, so the flag does nothing for these files. Portal also turns on `FLAG_WORKAROUND_IGNORE_TFDT_BOX`. That flag does nothing today because there is no `tfdt` box. It **will break seeking** once the muxer starts writing `tfdt` (see §4.2).

**Upstream status:** AndroidX Media `main` fixed this in the muxer after 1.11.1 (issue [#3208](https://github.com/androidx/media/issues/3208), closed 2026-07-28). `main` now writes `tfdt` in every `traf` and writes `mfra` at the end of the file. These changes are listed under **"Unreleased changes"** in the release notes. No Maven release contains them yet.

**Confirmed vs. not yet confirmed**
| Item | Status |
|---|---|
| `tfdt`, `mfra`, `sidx`, `mehd` are missing from real files | Confirmed by the writer source code (1.11.1). Not caused by MP4Box.js. Still to do: one `ffprobe`/box dump of a device recording (§7.1). |
| Missing index → `Unseekable` → seek fails | Confirmed by the extractor and player source code (1.11.1). Still to do: a device test (§7). |
| Zero durations in `mvhd`/`tkhd`/`mdhd`, missing `mehd` | Valid fMP4. Not a bug (§3.3). |
| A/V sync, fragment-boundary timing | Small risk, not confirmed. Measure it (§3.6, §7.4). |

Section 6 gives the fix plan. In short:
- **(A)** Use the first Media3 release (RC or stable) that contains the upstream `tfdt` + `mfra` writer.
- **(B)** If a fix must ship earlier, add a small wrapper around the output channel inside `03_camera` that adds `tfdt` and `mfra`.
- **(C)** In both cases, remove `FLAG_WORKAROUND_IGNORE_TFDT_BOX` from the Portal.
- **(D)** Optional: repair old recordings.

---

## 2. Research sources

Official source code and release notes:
- Media3 1.11.1 source JARs (used for every "1.11.1" statement below): `https://dl.google.com/android/maven2/androidx/media3/media3-muxer/1.11.1/`, `.../media3-extractor/1.11.1/`, `.../media3-exoplayer/1.11.1/`
- Media3 version list (latest = 1.11.1): https://dl.google.com/android/maven2/androidx/media3/media3-muxer/maven-metadata.xml
- AndroidX Media release notes (`main`): https://github.com/androidx/media/blob/main/RELEASENOTES.md
  - "Unreleased changes → Muxer": *"Write `tfdt` … box by default in `traf` boxes of fragmented MP4 files"* and *"Write `mfra` … box at the end of fragmented MP4 files to allow seeking via `FragmentedMp4Extractor` using `FLAG_READ_MFRA_FOR_SEEK_MAP`."*
  - 1.11.0 (2026-08-05): *"MP4: Add support for seeking in fragmented MP4 files using the `mfra` box when a `sidx` box is not present … now done by default in `DefaultExtractorsFactory`."*
- Upstream writer on `main`: https://github.com/androidx/media/blob/main/libraries/muxer/src/main/java/androidx/media3/muxer/FragmentedMp4Writer.java
- Upstream boxes on `main`: https://github.com/androidx/media/blob/main/libraries/muxer/src/main/java/androidx/media3/muxer/Boxes.java
- Upstream muxer API: https://github.com/androidx/media/blob/main/libraries/muxer/src/main/java/androidx/media3/muxer/FragmentedMp4Muxer.java
- Extractor: https://github.com/androidx/media/blob/main/libraries/extractor/src/main/java/androidx/media3/extractor/mp4/FragmentedMp4Extractor.java
- `FragmentedMp4Muxer` API reference: https://developer.android.com/reference/androidx/media3/muxer/FragmentedMp4Muxer
- `FragmentedMp4Extractor` API reference (flags): https://developer.android.com/reference/androidx/media3/extractor/mp4/FragmentedMp4Extractor
- `Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM`: https://developer.android.com/reference/androidx/media3/common/Player#COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM()
- ISO/IEC 14496-12 (ISOBMFF) §8.8 (movie fragments: `mehd` 8.8.2, `tfdt` 8.8.12, `mfra`/`tfra`/`mfro` 8.8.9–8.8.11): https://www.iso.org/standard/83102.html

GitHub issues:
- [androidx/media#3208](https://github.com/androidx/media/issues/3208): "FragmentedMp4Muxer: add support for sidx and mfra boxes to enable seekability in fragmented MP4 output". Closed 2026-07-28. This is the same problem: *"FragmentedMp4Extractor cannot treat these files as seekable"*.
- [androidx/media#3088](https://github.com/androidx/media/issues/3088): "FragmentedMp4Extractor marks fragmented MP4 as unseekable when mfra box is present but no sidx box". Fixed in 1.11.0.
- [androidx/media#3412](https://github.com/androidx/media/issues/3412): extractor failed on a negative version-1 `tfdt`. Fixed upstream. Relevant once `tfdt` is written.
- [androidx/media#3471](https://github.com/androidx/media/issues/3471) (open): `FragmentedMp4Extractor` crashes on a zero-sample `trun`. The 1.11.1 writer never writes an empty `traf` (it skips tracks with no pending samples). Any custom box rewriting must keep it that way.
- [androidx/media#2803](https://github.com/androidx/media/issues/2803): EOS before any sample crashed the writers. Fixed upstream.
- Historical extractor behaviour without `tfdt`: commit [`bfee449`](https://github.com/androidx/media/commit/bfee449ed8) "Fix FMP4 playback duration and absent tfdt handling".

---

## 3. Suspected issues, one by one

### 3.1 `tfdt` missing from `traf`
- **Evidence:** Media3 1.11.1 `Boxes.traf(ByteBuffer tfhdBox, ByteBuffer trunBox)` (`Boxes.java:1304`) only accepts `tfhd` + `trun`. The comment in `FragmentedMp4Writer.createFragment()` (`:263`) shows the layout as `moof{mfhd, traf{tfhd, trun}…}, mdat`. `03_camera` passes nothing that could add a box: `FragmentedMp4Muxer.Builder(channel).build()` at `FragmentedMedia3Muxer.kt:67` and `:92`.
- **Verdict:** The box is really missing from the file. MP4Box.js is showing it correctly.
- **Impact:** `tfdt` is optional in ISOBMFF. Media3's extractor works without it: `FragmentedMp4Extractor.java:1119–1128` continues from `TrackFragment.nextFragmentDecodeTime`, so normal playback has correct timestamps. After a seek, though, `TrackFragment.reset()` sets `nextFragmentDecodeTime = 0` (`TrackFragment.java:113`). Without `tfdt`, the first fragment read after a seek would start at time 0. **So adding only `mfra` is not enough. Seeking needs `tfdt` and an index together.** This is why upstream added both. Other players (browsers/MSE, some hardware players) depend more on `tfdt`.
- **Verdict:** A compatibility gap. It is part of the seek fix, but on its own it does not cause the current symptom.

### 3.2 `mfra` / `sidx` missing
- **Evidence:** The 1.11.1 `FragmentedMp4Writer.close()` (`:178`) only flushes the last fragment. Nothing writes `sidx`/`mfra`. In the extractor, the first `moof` arrives without a seek map. `FragmentedMp4Extractor.java:680` then emits `new SeekMap.Unseekable(durationUs, …)`. With `FLAG_READ_MFRA_FOR_SEEK_MAP`, it first reads the end of the file for `mfro`. When `mfro` is missing it falls back to `Unseekable` (`:2074–2208`).
- Player side: `Util.getAvailableCommands` adds `COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM` only if `isCurrentMediaItemSeekable`. `ProgressiveMediaPeriod.java:589` and `:633`: "Treat all seeks into non-seekable media as being to t=0".
- **Verdict: this is the root cause of the Portal symptom.**

### 3.3 Zero `mvhd`/`tkhd`/`mdhd` duration, no `mehd`
- **Evidence:** The fragmented writer writes `moov` before the first sample (`FragmentedMp4Writer.java:240`, `Boxes.moov(..., isFragmentedMp4 = true, ...)`). The total duration is not known at that point. ISOBMFF allows duration 0 and makes `mehd` optional. The Media3 extractor treats a zero `tkhd` duration as `C.TIME_UNSET` (`BoxParser.java:1112–1118`). For local files, `ProgressiveMediaPeriod` then takes the duration from the largest sample time once loading finishes (`:785`). Upstream `main` also writes `moov` with zero duration and no `mehd`.
- **Verdict: not a bug. No change is needed.** Writing `mehd` later would mean seeking back in the file, which defeats the purpose of append-only fMP4 (`isInterruptionResilient() = true`, `FragmentedMedia3Muxer.kt:142`). It is also not needed for seeking.

### 3.4 Fragment duration and sync samples
- **Evidence:** `DEFAULT_FRAGMENT_DURATION_MS = 2_000` (`FragmentedMp4Muxer.java:92`). `03_camera` does not override it. `shouldFlushPendingSamples()` (`FragmentedMp4Writer.java:244–262`) starts a new fragment only on a video keyframe, after at least 2 s of video is pending. So every fragment after the first starts with a sync sample. `trun` sets the sync flag per sample (`TRUN_BOX_SYNC_SAMPLE_FLAGS`, `Boxes.java:90`). The wrapper also marks every audio sample as a keyframe (`FragmentedMedia3Muxer.kt:377–378`). That is correct for AAC/Opus, and upstream does the same ("Set sync flag for audio samples in the trun box").
- **Verdict: correct. No change is needed.** With an index, seek precision depends on keyframe spacing, not on fragment spacing. The extractor jumps to the nearest `moof` from `tfra`. ExoPlayer then decodes forward to the exact position, because `SeekParameters.DEFAULT = EXACT` (`SeekParameters.java:56`).

### 3.5 Timestamp ordering
- **Evidence:** The wrapper requires presentation timestamps that never decrease (`FragmentedMedia3Muxer.kt:363`) and rejects B-frames, as 1.11.1 requires.
- **Verdict:** Correct for CameraX H.264/HEVC encoder output without B-frames. No change is needed.

### 3.6 A/V synchronisation (small risk, not confirmed)
- **Inference from source:** Without `tfdt`, every fragment's time is the running sum of the previous `trun` sample durations. 1.11.1 does not know the next fragment's first timestamp. So the duration of the last sample in each fragment is a guess. Upstream `main` changed this: it passes `nextFragmentFirstSampleTimeUs` into `processAllTracks`. If the guess is wrong (for example a dropped frame or an audio gap at a fragment boundary), the error adds up per track across fragments, and audio and video can drift slowly in long recordings. The `moov` is written before samples, so it also cannot record a start offset between the audio and video tracks (`Boxes.edts` returns an empty box when the first PTS is 0).
- Also, `FragmentedMedia3Muxer.kt:348` drops zero-size EOS buffers before Media3 sees them. So Media3 cannot use the EOS timestamp for the length of the very last sample.
- **Verdict:** Not confirmed. The size is unknown. Writing `tfdt` from real timestamps (upstream behaviour) fixes the drift problem. Measure it with §7.4 before changing anything for A/V sync alone.

### 3.7 Finalisation and interrupted recordings
- **Evidence:** `stop()` calls `muxer.close()` (`FragmentedMedia3Muxer.kt:404–409`), which flushes the last fragment. With upstream `main` it also writes `mfra`. If the process dies, the file ends after the last complete fragment (about 2 s of data may be lost) and stays playable. This is the point of fMP4.
- **Consequence for the fix:** An interrupted file never has `mfra`. So even after the fix, **interrupted recordings stay unseekable** in Media3 unless they are repaired (§6, Fix D). This must be documented and tested.

### 3.8 Portal player configuration
- `04_portal/.../ui/viewer/SpecificViewers.kt:121–124` (VideoViewer) and `:192–195` (AudioViewer) set `FLAG_READ_MFRA_FOR_SEEK_MAP | FLAG_WORKAROUND_IGNORE_TFDT_BOX | FLAG_ENABLE_EMSG_TRACK`.
  - `FLAG_READ_MFRA_FOR_SEEK_MAP`: correct, but already set by `DefaultExtractorsFactory` since 1.11.0 (`DefaultExtractorsFactory.java:555–556`). It does nothing until files contain `mfra`.
  - `FLAG_WORKAROUND_IGNORE_TFDT_BOX`: **harmful once `tfdt` exists.** After a seek, timestamps would restart at 0 (§3.1). It was made for SmoothStreaming. It should be removed.
  - `FLAG_ENABLE_EMSG_TRACK`: the recordings have no `emsg`. It does no harm and has nothing to do with seeking.
- Seek increments of 5 s back / 10 s forward (`:131–132`, `:202–203`) only affect the skip buttons, not dragging the progress bar.
- The content URI is file-backed and seekable: `02_vault/.../provider/MediaProvider.kt:86` `openFile` → `ParcelFileDescriptor.open(file, MODE_READ_ONLY)`. So the extractor can read `mfro` at the end of the file once it exists.
- No custom seek, buffering or timestamp-mapping logic exists in `MediaViewerViewModel.kt` or `MediaViewerScreen.kt`. **The Portal is not the root cause.** It only needs the flag cleanup.

---

## 4. What upstream changed (reference for Fix A/B)

4.1 `Boxes.traf(tfhd, tfdt, trun)` and `Boxes.tfdt(long baseMediaDecodeTime)` (version 1, 64-bit). `baseMediaDecodeTime = max(0, scale(firstPendingSample.presentationTimeUs, trackTimescale, 1_000_000))`. This uses absolute presentation time, clamped at 0.

4.2 When a fragment's first pending sample is a keyframe (video or audio), or belongs to a non-A/V track, `createFragment()` adds a `TfraEntry(time, moofOffset, trafNumber)` for that track. `close()` writes `mfra{tfra per track, mfro}`.

4.3 Once 4.1 exists, `FLAG_WORKAROUND_IGNORE_TFDT_BOX` makes `parseTraf` ignore the real `tfdt` (`FragmentedMp4Extractor.java:1124`). After an `mfra` seek, the timeline would restart at 0. **The Portal flag must be removed in the same release as the muxer fix.**

---

## 5. Exact change locations

| # | File | Location | Change |
|---|---|---|---|
| 1 | `03_camera/gradle/libs.versions.toml` | `:20` `media3 = "1.11.1"` | Upgrade to the first Media3 RC/stable that contains the "Write `tfdt`" and "Write `mfra`" release notes (Fix A). Never downgrade. |
| 2 | `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentedMedia3Muxer.kt` | `:67`, `:92` (`FragmentedMp4Muxer.Builder(channel)`) | Fix B only: wrap the channel in `FragmentIndexingChannel`, and finish it in `stop()` (`:404–409`) after `muxer.close()`. |
| 3 | new `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentIndexingChannel.kt` | — | Fix B only (see §6). Delete it after Fix A lands. |
| 4 | `04_portal/app/src/main/kotlin/rajnishkmehta/sakshi/portal/ui/viewer/SpecificViewers.kt` | `:121–124`, `:192–195` | Remove `FLAG_WORKAROUND_IGNORE_TFDT_BOX`. Optionally remove `FLAG_READ_MFRA_FOR_SEEK_MAP` (already the default) and `FLAG_ENABLE_EMSG_TRACK`, or keep them only with a comment. |
| 5 | `04_portal/gradle/libs.versions.toml` | `:2` | Keep the same Media3 version as the camera (at least 1.11.0 for mfra seeking, which is already met). |
| 6 | `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/ui/activities/VideoPlayer.kt` | `:138` | No functional change. Keep it free of `IGNORE_TFDT`. Remove the `// Just an example` comment if you touch the line. |
| 7 | `03_camera/app/src/main/java/rajnishkmehta/sakshi/camera/capturer/FragmentedMedia3Muxer.kt` | `:348` (EOS drop) | Only if §7.4 measures a final-sample/drift problem: pass the zero-size EOS sample to Media3 instead of returning early (Media3 1.11.1 `Track.java:84–86` records `endOfStreamTimestampUs` from a zero-size EOS buffer). |
| 8 | Docs (`03_camera` README / KDoc of `FragmentedMedia3Muxer`) | — | State that the output contains `tfdt` + `mfra`, and that interrupted files are playable but unseekable unless repaired. |

---

## 6. Step-by-step solution

### Fix A (recommended): upstream muxer
1. Watch https://github.com/androidx/media/blob/main/RELEASENOTES.md and Google Maven for the first version after 1.11.1 that lists the two muxer items (the 1.12.0 alpha/beta/RC line). Following AGENTS.md ("RC if stable unavailable"), use the RC if no stable release exists yet.
2. Change `media3` in **both** version catalogs.
3. Check API changes in `FragmentedMp4Muxer.Builder` and `Muxer` (for example the new `Muxer.Factory.create(ParcelFileDescriptor)`). `FragmentedMedia3Muxer` should keep using `Builder(channel)`. The `tfdt`/`mfra` writing is on by default, so no new options are needed.
4. Remove `FLAG_WORKAROUND_IGNORE_TFDT_BOX` from the Portal (table row 4).
5. Run §7.

### Fix B (only if a fix must ship before a Media3 release has it): `FragmentIndexingChannel`
A `WritableByteChannel` decorator around the real channel, given to `FragmentedMp4Muxer.Builder`. It keeps the output append-only, so recordings stay interruption-resilient.
1. **Parse top-level boxes from the stream** with a small state machine. Do not assume one `write()` call is one box. Buffer `moof` boxes fully. Pass `ftyp`, `moov` and `mdat` through, and remember the track IDs from `moov/trak/tkhd`.
2. **For each `moof`:** for every `traf`, read `tfhd.track_ID` and the `trun` sample_count and sample_durations. Insert `tfdt` (version 1, 20 bytes) right after `tfhd`, with `baseMediaDecodeTime = runningDecodeTime[trackId]`. Then `runningDecodeTime[trackId] += sum(sample_durations)`. This gives exactly the timeline that today's extractor builds during linear playback, so normal playback does not change.
3. **Fix the offsets**, because the `moof` grows by `delta = 20 × trafCount`. In 1.11.1, `tfhd.base_data_offset` is the absolute start position of the `moof` (flag `0x000001`, `FragmentedMp4Writer.java:198`), and `trun.data_offset` is measured from that `moof` start (`:191–193`). So:
   - Increase the `moof` size by `delta`, and each `traf` size by 20.
   - Set `tfhd.base_data_offset += cumulativeInsertedBefore`. This is the new `moof` position, because only earlier `moof`s grew.
   - Set `trun.data_offset += delta`, because the sample data moved further away from the `moof` start.
   - Write unit tests for both offsets: the first sample read must equal the first byte of its `mdat` payload.
4. **Record a random-access entry** when the `trun` first sample flags are sync (`0x02000000`): `(trackId, time = tfdt, moofOffset = rewrittenOutputPosition, trafNumber, trunNumber = 1, sampleNumber = 1)`.
5. **`finish()`** (called from `FragmentedMedia3Muxer.stop()` after `muxer.close()`, before closing the stream): append `mfra{ tfra(v1, length_size fields = 0) per track, mfro(size of mfra) }`. Leave out any `tfra` for a track with no entries.
6. Never write an empty `traf` or a `trun` with zero samples (#3471).
7. Delete Fix B when Fix A lands. Do not run both, or `tfdt` would be inserted twice. Add a guard: if a `traf` already has a `tfdt`, pass it through unchanged.

### Fix C: Portal flags
Remove `FLAG_WORKAROUND_IGNORE_TFDT_BOX` in `SpecificViewers.kt` (both viewers). Ship it together with or before A/B. With today's files it changes nothing, because there is no `tfdt`.

### Fix D (optional): repair old and interrupted recordings
Old recordings and interrupted recordings have no `mfra` and stay unseekable. Write a one-time copy-and-rewrite using the same logic as Fix B (steps 1–6) over an existing file, writing to a temp file and then renaming it. Run it on recordings without `mfro` (check the last 16 bytes for `mfro`). Where to run it is a product decision: the camera on next start, or the Vault on import.

---

## 7. Test plan

### 7.1 Structure (shell, on a pulled recording)
```bash
ffprobe -v trace rec.mp4 2>&1 | grep -E "type:'(moov|mvex|mehd|moof|traf|tfhd|tfdt|trun|mfra|tfra|mfro|sidx)'"
```
Or use `MP4Box -info -v rec.mp4` / `mp4dump rec.mp4` (Bento4).
- Before the fix: no `tfdt`, `mfra` or `sidx` (this confirms §3.1/3.2 on a real device file).
- After the fix: every `traf` has `tfdt`. The file ends with `mfra` and a 16-byte `mfro`. `mfro.size` equals the `mfra` size. Every `tfra.moof_offset` points to a `moof` box.

### 7.2 Unit tests (`03_camera/app/src/test`, Robolectric + `androidx.media3:media3-test-utils` via the version catalog)
1. Feed `FragmentedMedia3Muxer` 10 s of synthetic H.264 (keyframe every 1 s, 30 fps) and AAC samples, written to a temp file.
2. Box parse: check `tfdt` is present, increasing per track, and `tfdt[n+1] = tfdt[n] + Σ trun durations[n]` (Fix B) or ≈ first-sample PTS (Fix A). Check `mfra`/`mfro` sizes and offsets.
3. Run `FragmentedMp4Extractor` (default flags, plus the Portal flags after Fix C) with `FakeExtractorOutput`. Check `seekMap.isSeekable() == true` and `durationUs` ≈ 10 s.
4. For targets {2 s, 4 s, 6 s → back to 2 s}: `seekMap.getSeekPoints(t)` returns a point with time ≤ t at a keyframe fragment. After `extractor.seek(position, t)`, the first output sample time equals the fragment's `tfdt` (not 0).
5. Interrupted file: cut the file at the end of the 3rd fragment. The extractor still outputs samples, `isSeekable == false`, and nothing crashes.
6. Regression: Portal flags with `IGNORE_TFDT` give wrong timestamps after a seek. This proves why Fix C is needed.

### 7.3 Device tests (Portal)
- Record 60 s fMP4. In the Portal, drag the progress bar +2 s, +4 s, −2 s, −4 s, to the middle and near the end. Use the 5 s/10 s skip buttons. Expected: the position shown matches the requested time within one frame after the seek finishes, with no jump to 0.
- Repeat for 4K/HEVC, the audio-only `AudioViewer`, and long recordings (30 min or more).
- Check the duration shown in the Portal, Camera `VideoPlayer`, Google Photos/VLC/Chrome.

### 7.4 A/V sync
Record a clapper or beep with a flash for 10 min. Use `ffprobe -show_frames -select_streams v/a` to get the PTS of the first and last audio and video samples. In the Portal, seek to the 1, 5 and 9 minute marks and confirm the offset stays within 40 ms (about one video frame). Compare before and after the fix to decide whether table row 7 is needed.

### 7.5 Finalisation and interruption
- A normal stop, the size limit, the duration limit and `ERROR_SOURCE_INACTIVE` (`VideoCapturer.kt:343–346`) must all produce `mfra`.
- Kill the process with `adb shell am force-stop` during recording: the file is playable up to the last fragment, unseekable before Fix D, and seekable after Fix D.
- Low-storage stop (`ERROR_INSUFFICIENT_STORAGE`) behaves the same as an interrupted file.

---

## 8. Things that do NOT need a change
- Zero `mvhd`/`tkhd`/`mdhd` durations and the missing `mehd` (§3.3).
- Fragment length and keyframe-aligned fragments (§3.4).
- Marking audio samples as sync samples (§3.4).
- Portal seek increments, buffering and the content provider (§3.8).
- `sidx`. `mfra` is enough for Media3 since 1.11.0 (#3088). `sidx` would need to be written at the start of the file, which conflicts with append-only recording, and upstream chose `mfra` for the same reason (#3208).
