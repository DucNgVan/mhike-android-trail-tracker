package com.example.hiking_application_coursework.helpers;

import com.example.hiking_application_coursework.models.Hike;
import com.example.hiking_application_coursework.models.Observation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreHelper {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference hikesRef = db.collection("hikes");
    private final CollectionReference obsRef = db.collection("observations");

    public interface DataStatus {
        void dataIsLoaded(List<Hike> hikes);
        void dataIsInserted();
        void dataIsDeleted();
    }

    public interface ObservationStatus {
        void observationsLoaded(List<Observation> observations);
    }

    public void getAllHikes(final DataStatus status) {
        hikesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Hike> hikes = new ArrayList<>();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Hike hike = document.toObject(Hike.class);
                hike.setFirebaseId(document.getId());
                hikes.add(hike);
            }
            status.dataIsLoaded(hikes);
        });
    }

    public void insertHike(Hike hike, final DataStatus status) {
        hikesRef.add(hike).addOnSuccessListener(documentReference -> status.dataIsInserted());
    }

    public void updateHike(Hike hike, final DataStatus status) {
        if (hike.getFirebaseId() != null) {
            hikesRef.document(hike.getFirebaseId()).set(hike)
                    .addOnSuccessListener(aVoid -> status.dataIsInserted());
        }
    }

    public void deleteHike(String id, final DataStatus status) {
        hikesRef.document(id).delete().addOnSuccessListener(aVoid -> status.dataIsDeleted());
    }

    public void insertObservation(Observation observation, OnSuccessListener<DocumentReference> listener) {
        obsRef.add(observation).addOnSuccessListener(listener);
    }

    public void getObservationsForHike(String hikeId, final ObservationStatus status) {
        obsRef.whereEqualTo("hikeFirebaseId", hikeId).get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Observation> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                list.add(doc.toObject(Observation.class));
            }
            status.observationsLoaded(list);
        });
    }

    /**
     * XOÁ TOÀN BỘ DỮ LIỆU ĐỂ LÀM MỚI (WIPE ALL)
     */
    public void wipeAllData(final Runnable onComplete) {
        // 1. Xoá tất cả Hikes
        hikesRef.get().addOnSuccessListener(hikeSnaps -> {
            for (QueryDocumentSnapshot doc : hikeSnaps) {
                doc.getReference().delete();
            }
            // 2. Xoá tất cả Observations
            obsRef.get().addOnSuccessListener(obsSnaps -> {
                for (QueryDocumentSnapshot doc : obsSnaps) {
                    doc.getReference().delete();
                }
                if (onComplete != null) onComplete.run();
            });
        });
    }

    /**
     * TẠO DỮ LIỆU MẪU ĐỊA ĐIỂM VIỆT NAM (SAMPLE VIETNAM HIKES SEEDER)
     */
    public void seedSampleVietnamData(String creatorId, final Runnable onComplete) {
        wipeAllData(() -> {
            List<Hike> samples = new ArrayList<>();

            Hike h1 = new Hike();
            h1.setName("Fansipan Summit Conquest"); h1.setLocation("Rung Quoc Gia Hoang Lien, Sapa, Lao Cai"); h1.setDate("12/10/2025"); h1.setParking("Yes"); h1.setLength("12.5"); h1.setDifficulty("Hard"); h1.setDescription("Conquering the roof of Indochina at 3,143m altitude. Steep trails, cloud hunting, and mountain vistas.");
            h1.setDuration("8.0"); h1.setWeather("Windy & Misty"); h1.setImageResName("img1"); h1.setCreatorId(creatorId);
            List<Map<String, Double>> r1 = new ArrayList<>(); Map<String, Double> p1 = new HashMap<>(); p1.put("lat", 22.303333); p1.put("lng", 103.775000); r1.add(p1); h1.setPlannedRoute(r1);
            samples.add(h1);

            Hike h2 = new Hike();
            h2.setName("Son Doong Cave Expedition"); h2.setLocation("Phong Nha - Ke Bang National Park, Quang Binh"); h2.setDate("15/11/2025"); h2.setParking("Yes"); h2.setLength("25.0"); h2.setDifficulty("Hard"); h2.setDescription("Exploring the world's largest cave passage with subterranean rainforests and giant stalagmites.");
            h2.setDuration("14.0"); h2.setWeather("Humid & Cool"); h2.setImageResName("img2"); h2.setCreatorId(creatorId);
            List<Map<String, Double>> r2 = new ArrayList<>(); Map<String, Double> p2 = new HashMap<>(); p2.put("lat", 17.462500); p2.put("lng", 106.287500); r2.add(p2); h2.setPlannedRoute(r2);
            samples.add(h2);

            Hike h3 = new Hike();
            h3.setName("Langbiang Mountain Trail"); h3.setLocation("Langbiang Mountain, Da Lat, Lam Dong"); h3.setDate("20/09/2025"); h3.setParking("Yes"); h3.setLength("9.2"); h3.setDifficulty("Medium"); h3.setDescription("Hiking through pine forests and wild flower valleys to the 2,167m peak overlooking Da Lat city.");
            h3.setDuration("4.5"); h3.setWeather("Sunny & Breezy"); h3.setImageResName("img3"); h3.setCreatorId(creatorId);
            List<Map<String, Double>> r3 = new ArrayList<>(); Map<String, Double> p3 = new HashMap<>(); p3.put("lat", 12.045000); p3.put("lng", 108.437000); r3.add(p3); h3.setPlannedRoute(r3);
            samples.add(h3);

            Hike h4 = new Hike();
            h4.setName("Marble Mountains Exploration"); h4.setLocation("Ngu Hanh Son, Da Nang"); h4.setDate("05/08/2025"); h4.setParking("Yes"); h4.setLength("4.5"); h4.setDifficulty("Easy"); h4.setDescription("Scenic limestone hill trail with ancient Buddhist pagodas, hidden grottoes, and ocean views.");
            h4.setDuration("2.5"); h4.setWeather("Sunny"); h4.setImageResName("img4"); h4.setCreatorId(creatorId);
            List<Map<String, Double>> r4 = new ArrayList<>(); Map<String, Double> p4 = new HashMap<>(); p4.put("lat", 16.002200); p4.put("lng", 108.263500); r4.add(p4); h4.setPlannedRoute(r4);
            samples.add(h4);

            Hike h5 = new Hike();
            h5.setName("Ta Nang - Phan Dung Trekking"); h5.setLocation("Ta Nang, Duc Trong, Lam Dong"); h5.setDate("01/12/2025"); h5.setParking("Yes"); h5.setLength("32.0"); h5.setDifficulty("Hard"); h5.setDescription("Vietnam's most legendary grass hill trekking trail passing through rolling green meadows and pine ridges.");
            h5.setDuration("16.0"); h5.setWeather("Sunny & Clear"); h5.setImageResName("img5"); h5.setCreatorId(creatorId);
            List<Map<String, Double>> r5 = new ArrayList<>(); Map<String, Double> p5 = new HashMap<>(); p5.put("lat", 11.530000); p5.put("lng", 108.520000); r5.add(p5); h5.setPlannedRoute(r5);
            samples.add(h5);

            Hike h6 = new Hike();
            h6.setName("Ba Den Mountain Peak"); h6.setLocation("Núi Bà Đen, Tay Ninh"); h6.setDate("10/01/2026"); h6.setParking("Yes"); h6.setLength("7.8"); h6.setDifficulty("Medium"); h6.setDescription("Conquering Southeastern Vietnam's highest mountain at 986m altitude with sacred temple stops.");
            h6.setDuration("5.0"); h6.setWeather("Warm & Sunny"); h6.setImageResName("img1"); h6.setCreatorId(creatorId);
            List<Map<String, Double>> r6 = new ArrayList<>(); Map<String, Double> p6 = new HashMap<>(); p6.put("lat", 11.378000); p6.put("lng", 106.168000); r6.add(p6); h6.setPlannedRoute(r6);
            samples.add(h6);

            int[] count = {0};
            for (Hike h : samples) {
                hikesRef.add(h).addOnSuccessListener(doc -> {
                    h.setFirebaseId(doc.getId());
                    hikesRef.document(doc.getId()).set(h);
                    
                    // Add sample field observation
                    Observation obs = new Observation();
                    obs.setHikeFirebaseId(doc.getId());
                    obs.setObservation("Perfect trekking conditions, scenic mountain checkpoint!");
                    obs.setTime("09:30 AM");
                    obs.setComments("Clear visibility and fresh mountain air.");
                    obsRef.add(obs);

                    count[0]++;
                    if (count[0] == samples.size() && onComplete != null) {
                        onComplete.run();
                    }
                });
            }
        });
    }
}
