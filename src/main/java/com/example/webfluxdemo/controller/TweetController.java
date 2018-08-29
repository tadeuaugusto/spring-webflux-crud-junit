package com.example.webfluxdemo.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.webfluxdemo.model.Tweet;
import com.example.webfluxdemo.repository.TweetRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * https://www.callicoder.com/reactive-rest-apis-spring-webflux-reactive-mongo/
 * @author Tadeu
 *
 */
@RestController
public class TweetController {

	@Autowired
	private TweetRepository tweetRepository;

	/**
	 * Return is similar to Server-Sent-Events but without extra information.
	 * Ex:
	 * {"id":"59ba5389d2b2a85ed4ebdafa","text":"tweet1","createdAt":1505383305602}
	 * {"id":"59ba5587d2b2a85f93b8ece7","text":"tweet2","createdAt":1505383814847}
	 * @return
	 */
	@GetMapping("/tweets")
	public Flux<Tweet> getAllTweets() {
		return tweetRepository.findAll();
	}
	
	@PostMapping("/tweets")
	public Mono<Tweet> createTweets(@Valid @RequestBody Tweet tweet) {
		return tweetRepository.save(tweet);
	}
	
	@GetMapping("/tweets/{id}")
	public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String tweetId) {
		return tweetRepository.findById(tweetId)
				.map(savedTweet -> ResponseEntity.ok(savedTweet))
				.defaultIfEmpty(ResponseEntity.notFound().build());
				// .map(savedTweet -> new ResponseEntity<Tweet>(savedTweet, HttpStatus.OK))
				// .defaultIfEmpty(new ResponseEntity<Tweet>(HttpStatus.NOT_FOUND));
	}
	
	// ...
	
	@PutMapping("/tweets/{id}")
	public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable(value = "id") String tweetId,
													@Valid @RequestBody Tweet tweet) {
		return tweetRepository.findById(tweetId)
				.flatMap(existingTweet -> {
					existingTweet.setText(tweet.getText());
					return tweetRepository.save(existingTweet);
				})
				// .map(updatedTweet -> ResponseEntity.ok(updatedTweet))
				// .defaultIfEmpty(ResponseEntity.notFound().build());
				.map(updatedTweet -> new ResponseEntity<Tweet>(updatedTweet, HttpStatus.OK))
				.defaultIfEmpty(new ResponseEntity<Tweet>(HttpStatus.NOT_FOUND));
	}
	
	@DeleteMapping("/tweets/{id}")
	public Mono<ResponseEntity<Void>> deleteTweet(@PathVariable(value = "id") String tweetId) {
		return tweetRepository.findById(tweetId)
				.flatMap(existingTweet -> 
					tweetRepository.delete(existingTweet)
						.then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
				)
				.defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
	
	/**
	 * Send tweets to the client as Server Sent Events
	 * Ex:
	 * data: {"id":"59ba5389d2b2a85ed4ebdafa","text":"tweet1","createdAt":1505383305602}
	 * data: {"id":"59ba5587d2b2a85f93b8ece7","text":"tweet2","createdAt":1505383814847}
	 * @return
	 */
	@GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<Tweet> streamAllTweets() {
		return tweetRepository.findAll();
	}
	
	
}
