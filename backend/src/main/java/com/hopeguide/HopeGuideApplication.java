package com.hopeguide;

import com.hopeguide.entity.GuideResource;
import com.hopeguide.entity.User;
import com.hopeguide.repository.GuideResourceRepository;
import com.hopeguide.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@SpringBootApplication
public class HopeGuideApplication {

    public static void main(String[] args) {
        SpringApplication.run(HopeGuideApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(UserRepository userRepo,
                               GuideResourceRepository resourceRepo,
                               PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() == 0) {
                User u1 = new User();
                u1.setName("Jordan Lee");
                u1.setEmail("user@hopeguide.com");
                u1.setPasswordHash(encoder.encode("Demo@123"));
                u1.setRole("INDIVIDUAL");
                u1.setPreferredLanguage("en");
                userRepo.save(u1);

                User u2 = new User();
                u2.setName("Morgan Smith");
                u2.setEmail("family@hopeguide.com");
                u2.setPasswordHash(encoder.encode("Demo@123"));
                u2.setRole("FAMILY");
                u2.setPreferredLanguage("en");
                userRepo.save(u2);
            }

            if (resourceRepo.count() == 0) {
                List<GuideResource> resources = List.of(
                    new GuideResource(null, "Recognizing Relapse Warning Signs", "RELAPSE",
                        "SAMHSA", "https://www.samhsa.gov",
                        "Early warning signs of relapse include increased stress, reconnecting with people linked to substance use, withdrawing from support networks, and romanticizing past use. Early recognition allows for timely intervention."),
                    new GuideResource(null, "Breathing & Grounding Techniques", "COPING",
                        "NIDA", "https://www.drugabuse.gov",
                        "Box breathing (inhale 4s, hold 4s, exhale 4s, hold 4s) and the 5-4-3-2-1 grounding method are proven techniques to reduce acute craving intensity and manage overwhelming emotions in real time."),
                    new GuideResource(null, "Opioid Overdose: Signs & First Response", "OVERDOSE",
                        "CDC", "https://www.cdc.gov",
                        "Signs include pinpoint pupils, unconsciousness, and slow or stopped breathing. Administer naloxone if available, call 911 immediately, and perform rescue breathing until help arrives."),
                    new GuideResource(null, "How Families Can Help Without Enabling", "FAMILY",
                        "SAMHSA", "https://www.samhsa.gov",
                        "Set loving but firm boundaries. Avoid covering up consequences of substance use. Support treatment-seeking behavior. Consider joining Al-Anon or Nar-Anon for peer family support."),
                    new GuideResource(null, "Medication-Assisted Treatment (MAT)", "TREATMENT",
                        "SAMHSA", "https://www.samhsa.gov",
                        "MAT uses FDA-approved medications such as buprenorphine, methadone, or naltrexone alongside counseling to treat opioid use disorder. It significantly reduces overdose risk and supports long-term recovery."),
                    new GuideResource(null, "Building Resilience in Recovery", "RECOVERY",
                        "WHO", "https://www.who.int",
                        "Resilience in recovery is built through routine, meaningful connection, physical activity, and purpose. Peer support programs like SMART Recovery and 12-step groups provide accountability and belonging."),
                    new GuideResource(null, "Mindfulness-Based Relapse Prevention", "COPING",
                        "NIDA", "https://www.drugabuse.gov",
                        "MBRP teaches individuals to observe cravings without judgment or reaction. Regular 10-minute mindfulness sessions reduce urge intensity over time and strengthen emotional self-regulation."),
                    new GuideResource(null, "Harm Reduction Strategies", "RECOVERY",
                        "WHO", "https://www.who.int",
                        "Harm reduction approaches minimize health risks without requiring immediate abstinence. These include needle exchange programs, naloxone distribution, supervised consumption sites, and medication-assisted approaches.")
                );
                resourceRepo.saveAll(resources);
            }
        };
    }
}
