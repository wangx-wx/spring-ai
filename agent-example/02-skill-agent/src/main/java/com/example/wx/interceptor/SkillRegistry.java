package com.example.wx.interceptor;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangx
 * @description
 * @create 2026/1/14 21:33
 */
@Slf4j
public class SkillRegistry {
    private final Map<String, SkillMetadata> skills = new ConcurrentHashMap<>();


    public void register(SkillMetadata skill) {
        if (skill == null) {
            throw new IllegalArgumentException("Skill cannot be null");
        }

        String name = skill.getName();
        if (skills.containsKey(name)) {
            log.warn("Skill '{}' is already registered, overwriting", name);
        }

        skills.put(name, skill);
        log.debug("Registered skill: {}", name);
    }

    public void registerAll(List<SkillMetadata> skillList) {
        if (skillList == null) {
            return;
        }
        skillList.forEach(this::register);
    }

    public Optional<SkillMetadata> get(String name) {
        return Optional.ofNullable(skills.get(name));
    }

    public List<SkillMetadata> listAll() {
        return new ArrayList<>(skills.values());
    }

    public boolean contains(String name) {
        return skills.containsKey(name);
    }

    public int size() {
        return skills.size();
    }

    public void clear() {
        skills.clear();
        log.debug("Cleared all skills");
    }

    boolean unregister(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Skill name cannot be null or empty");
        }

        SkillMetadata removed = skills.remove(name);
        if (removed != null) {
            log.info("Unregistered skill: {}", name);
            return true;
        } else {
            log.debug("Attempted to unregister non-existent skill: {}", name);
            return false;
        }
    }

}
