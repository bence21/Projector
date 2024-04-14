//package com.bence.projector.server;
//
//import com.bence.projector.server.backend.model.SongVerse;
//import com.bence.projector.server.backend.service.SongService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import projector.model.Song;
//import projector.service.ServiceManeger;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//@SpringBootApplication
//public class ConvertSongs {
//
//	public static void main(String[] args) {
//		SpringApplication.run(ConvertSongs.class, args);
//	}
//
//	@Bean
//	public CommandLineRunner demo(SongService songService) {
//		return (String... args) -> {
//			final Date createdDate = new Date();
//			final List<Song> songList = ServiceManeger.getSongService().findAll();
//			ArrayList<com.bence.projector.server.backend.model.Song> convertedSongs = new ArrayList<>(songList.size());
//			for (Song song : songList) {
//				final com.bence.projector.server.backend.model.Song convertedSong = new com.bence.projector.server.backend.model.Song();
//				convertedSong.setTitle(song.getTitle());
//				convertedSong.setCreatedDate(createdDate);
//				convertedSong.setModifiedDate(createdDate);
//				ArrayList<SongVerse> convertedVerses = new ArrayList<>(song.getVerses().length);
//				for (projector.model.SongVerse songVerse : song.getVerses()) {
//					if (!songVerse.isRepeated()) {
//						final SongVerse verse = new SongVerse();
//						verse.setText(songVerse.getText());
//						verse.setChorus(songVerse.isChorus());
//						convertedVerses.add(verse);
//					}
//				}
//				convertedSong.setVerses(convertedVerses);
//				convertedSongs.add(convertedSong);
//			}
//			songService.save(convertedSongs);
//		};
//	}
//}
