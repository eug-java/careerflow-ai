package com.careerflow.job.entity;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class JobEntityLifecycleTest {

    @Test
    void jobDescriptionPrePersistShouldSetIdAndCreatedAt() throws Exception {
        JobDescription job = new JobDescription();

        Method method = JobDescription.class.getDeclaredMethod("prePersist");
        method.setAccessible(true);
        method.invoke(job);

        assertThat(job.getId()).isNotNull();
        assertThat(job.getCreatedAt()).isNotNull();
    }

    @Test
    void jobSkillPrePersistShouldSetId() throws Exception {
        JobSkill skill = new JobSkill();

        Method method = JobSkill.class.getDeclaredMethod("prePersist");
        method.setAccessible(true);
        method.invoke(skill);

        assertThat(skill.getId()).isNotNull();
    }
}
