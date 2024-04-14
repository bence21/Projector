//package com.bence.projector.server;
//
//import com.bence.projector.server.backend.model.Song;
//import com.bence.projector.server.backend.model.SongCollection;
//import com.bence.projector.server.backend.model.SongCollectionElement;
//import com.bence.projector.server.backend.repository.SongRepository;
//import com.bence.projector.server.backend.service.SongCollectionService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.Date;
//import java.util.List;
//
//@SpringBootApplication
//public class CreateSongCollections {
//
//    public static void main(String[] args) {
//        SpringApplication.run(CreateSongCollections.class, args);
//    }
//
//    @Bean
//    public CommandLineRunner demo(SongRepository songRepository, SongCollectionService songCollectionService) {
//        return (String... args) -> {
//            final Date createdDate = new Date();
//            List<Song> songs = songRepository.findAll();
//            songs.sort(Comparator.comparing(Song::getTitle));
//            SongCollection songCollection = new SongCollection();
//            songCollection.setCreatedDate(createdDate);
//            songCollection.setModifiedDate(createdDate);
////            songCollection.setName("Hit hangjai");
////            songCollection.setName("Gyűlekezeti énekeskönyv");
////            songCollection.setName("Régi gyűlekezeti énekeskönyv");
////            songCollection.setName("Pe drumul credinței");
////            songCollection.setName("Karénekes");
////            songCollection.setName("Halleluja");
//            songCollection.setName("Others with i");
//            List<SongCollectionElement> songCollectionElements = songCollection.getSongCollectionElements();
//            int count = 0;
//            List<Song> needToUpdateSongs = new ArrayList<>();
//            for (Song song : songs) {
//                ++count;
//                String title = song.getTitle();
//                String s = title.split(" ")[0];
//                if (s.equals("i")) {
//                    SongCollectionElement songCollectionElement = new SongCollectionElement();
//                    songCollectionElement.setOrdinalNumber(s);
//                    songCollectionElement.setSongUuid(song.getId());
//                    try {
//                        song.setTitle(title.substring(s.length() + 1, title.length()));
//                    } catch (Exception e) {
//                        System.out.println(e.getMessage());
//                        song.setTitle(song.getVerses().get(0).getText().split("\n")[0]);
//                    }
//                    songCollectionElements.add(songCollectionElement);
//                    needToUpdateSongs.add(song);
//                }
//            }
////            if (1 + 2 == 2 + 3 + count) {
//            songRepository.save(needToUpdateSongs);
//            songCollectionService.save(songCollection);
////            }
//        };
//    }
//
//    private boolean containsDigit(String s) {
//        char[] chars = s.toCharArray();
//        for (char aChar : chars) {
//            if (aChar >= '0' && aChar <= '9') {
//                return true;
//            }
//        }
//        return false;
//    }
//}