package com.epam.company.service.impl;

import com.epam.company.model.dao.Participant;
import com.epam.company.model.dto.TaskDTO;
import com.epam.company.service.ISubscription;
import com.epam.company.service.ISubscriptionRemove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by @belrbeZ
 */
@Service
@Primary
public class SubscriptionService implements ISubscription, ISubscriptionRemove {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    private final ParticipantService partService;

    @Autowired
    public SubscriptionService(ParticipantService partService) {
        this.partService = partService;
    }

    @Override
    public Optional<List<Long>> getTaskSubscribers(Long taskId) {
        Optional<List<Participant>> participants = partService.getParticipantByTask(taskId);
        if(participants.isPresent())
            return Optional.of(participants.get().stream().map(Participant::getUserId).collect(Collectors.toList()));
        return Optional.empty();
    }

    @Override
    public Optional<Set<Long>> getUserSubscriptions(Long userId) {
        Optional<List<Participant>> participants = partService.getParticipantByUser(userId);
        return (participants.isPresent())
                ? Optional.of(participants.get().stream().map(Participant::getTaskId).collect(Collectors.toSet()))
                : Optional.empty();
    }



    @Transactional
    @Override
    public Optional<Long> viewTask(Long userId, Long taskId) {
        Optional<Participant> participant = partService.getSpecificParticipant(userId, taskId);

        participant.ifPresent(p -> {
            p.reset();
            partService.save(p);
        });

        return (participant.isPresent())
                ? Optional.of(taskId)
                : Optional.empty();
    }

    @Transactional
    @Override
    public Optional<Long> modifyTask(Long taskId) {
        Optional<List<Participant>> participant = partService.getParticipantByTask(taskId);
        participant.ifPresent(p -> {
            p.forEach(Participant::modify);
            partService.save(p);
        });
        return (participant.isPresent())
                ? Optional.of(taskId)
                : Optional.empty();
    }

    /*@Transactional
    @Override
    public List<TaskDTO> fillSubs(Long userId, List<TaskDTO> tasks) {
        final List<TaskDTO> tasksToCounter = new ArrayList<>(tasks.stream().map(TaskDTO::new).collect(Collectors.toSet()));
        try {
            tasksToCounter.forEach(t -> partService.getSpecificParticipant(userId, t.getId())
                    .ifPresent(p -> t.setModifyCount(p.getModifyCounter())));
        } catch (Exception e) {
            logger.warn("NO SUBSCRIPTIONS [ERROR]");
        }

        return tasksToCounter;
    }

    @Transactional
    @Override
    public TaskDTO fillSubs(Long userId, TaskDTO task) {
        final TaskDTO taskToModify = new TaskDTO(task);
        try {
            partService.getSpecificParticipant(userId, taskToModify.getId())
                    .ifPresent(p -> taskToModify.setModifyCount(p.getModifyCounter()));
        } catch (Exception e) {
            logger.warn("NO SUBSCRIPTION [ERROR]");
        }

        return taskToModify;
    }*/

    @Transactional
    @Override
    public Optional<Long> subscribe(Long userId, Long taskId) {
        try {
            return (partService.saveParticipant(userId, taskId).isPresent())
                    ? Optional.of(taskId)
                    : Optional.empty();
        }
        catch (Exception e) {
            logger.warn("CAN'T SUBSCRIPTION [ERROR] : " + e.getMessage());
        }

        return Optional.empty();
    }

    @Transactional
    @Override
    public Optional<Long> unSubscribe(Long userId, Long taskId) {
        try {
            return (partService.removeSpecificParticipant(userId, taskId).isPresent())
                    ? Optional.of(taskId)
                    : Optional.empty();
        }
        catch (Exception e) {
            logger.warn("CAN'T UN SUBSCRIPTION [ERROR] : " + e.getMessage());
        }

        return Optional.empty();
    }

    @Transactional
    @Override
    public Optional<Long> removeUserSubscriptions(Long userId) {
        return (partService.removeParticipantsByUser(userId).isPresent())
                ? Optional.of(userId)
                : Optional.empty();
    }

    @Transactional
    @Override
    public Optional<Long> removeTaskSubscribers(Long taskId) {
        return (partService.removeParticipantsByTask(taskId).isPresent())
                ? Optional.of(taskId)
                : Optional.empty();
    }
}
