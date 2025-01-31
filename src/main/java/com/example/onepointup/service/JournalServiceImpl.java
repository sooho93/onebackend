package com.example.onepointup.service;

import com.example.onepointup.dto.JournalDTO;
import com.example.onepointup.model.Challenge;
import com.example.onepointup.model.Journal;
import com.example.onepointup.model.Mood;
import com.example.onepointup.repository.ChallengeRepository;
import com.example.onepointup.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
@Log4j2
@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {

    private final JournalRepository journalRepository;
    private final ChallengeRepository challengeRepository;

    @Override
    public JournalDTO createJournal(JournalDTO journalDTO) {
        log.info("createJournal"+journalDTO.getChallengeId());
        Challenge challenge = challengeRepository.findById(journalDTO.getChallengeId())
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        float updatedProgress = Math.min(challenge.getProgress() + journalDTO.getProgress(), 100);
        challenge.setProgress(updatedProgress);

        if (updatedProgress == 100) {
            challenge.setIsCompleted(true);
        }
        challengeRepository.save(challenge);

        Journal journal = Journal.builder()
                .challenge(challenge)
                .content(journalDTO.getContent())
                .mood(Mood.valueOf(journalDTO.getMood()))
                .progress(journalDTO.getProgress())
                .date(LocalDate.now())
                .build();

        journal = journalRepository.save(journal);
        return toDTO(journal);
    }

    @Override
    public JournalDTO getJournalById(Long journalId) {
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));
        return toDTO(journal);
    }

    @Override
    public List<JournalDTO> getJournalsByChallenge(Long challengeId) {
        List<Journal> journals = journalRepository.findByChallengeId(challengeId);
        return journals.stream().map(this::toDTO).toList();
    }



    @Override
    public void deleteJournal(Long journalId) {
        log.info("Deleting Journal with ID: " + journalId);
        Journal journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        // 기록 삭제
        journalRepository.delete(journal);

        // Challenge progress 재계산
        Challenge challenge = journal.getChallenge();
        float newProgress = challengeRepository.getTotalProgressByChallengeId(challenge.getId());
        challenge.setProgress(Math.min(newProgress, 100));
        challenge.setIsCompleted(newProgress == 100);
        challengeRepository.save(challenge);
    }


    private float calculateTotalProgress(Long challengeId) {
        return challengeRepository.getTotalProgressByChallengeId(challengeId);
    }


    private JournalDTO toDTO(Journal journal) {
        return JournalDTO.builder()
                .id(journal.getId())
                .challengeId(journal.getChallenge().getId())
                .content(journal.getContent())
                .mood(journal.getMood().name())
                .progress(journal.getProgress())
                .createdAt(journal.getCreatedAt())
                .build();
    }
}
