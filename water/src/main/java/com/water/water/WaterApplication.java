package com.water.water;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.water.water.model.User;
import com.water.water.repository.UserRepository;
import com.water.water.repository.ApartmentRepository;
import com.water.water.repository.HouseholdRepository;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;

@SpringBootApplication
@EnableScheduling
public class WaterApplication {

	public static void main(String[] args) {
		SpringApplication.run(WaterApplication.class, args);
	}

	/**
	 * Waits for Hibernate ddl-auto to finish creating the 'users' table.
	 * Uses exponential backoff: 2s, 4s, 8s, 16s, 30s (max 60s total).
	 * This prevents the "relation 'users' does not exist" error on Render
	 * free-tier where the PostgreSQL DB wakes up slowly from sleep.
	 */
	private boolean waitForSchema(DataSource dataSource) {
		int maxAttempts = 8;
		long waitMs = 2000; // start at 2 seconds

		for (int attempt = 1; attempt <= maxAttempts; attempt++) {
			try (Connection conn = dataSource.getConnection()) {
				DatabaseMetaData meta = conn.getMetaData();
				// Check for 'users' table (case-insensitive for both MySQL and PostgreSQL)
				try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
					while (rs.next()) {
						String tableName = rs.getString("TABLE_NAME");
						if ("users".equalsIgnoreCase(tableName)) {
							System.out.println("Bootstrap: Schema ready — 'users' table found (attempt " + attempt + ")");
							return true;
						}
					}
				}
				System.out.println("Bootstrap: Schema not ready yet (attempt " + attempt + "/" + maxAttempts + "), retrying in " + waitMs + "ms...");
			} catch (Exception e) {
				System.out.println("Bootstrap: DB connection not ready (attempt " + attempt + "/" + maxAttempts + "): " + e.getMessage());
			}

			try {
				Thread.sleep(waitMs);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				return false;
			}
			waitMs = Math.min(waitMs * 2, 30000); // exponential backoff, cap at 30s
		}
		System.err.println("Bootstrap: Schema NOT ready after all attempts — skipping bootstrap tasks.");
		return false;
	}

	@Bean
	public CommandLineRunner bootstrap(
			DataSource dataSource,
			UserRepository userRepository,
			ApartmentRepository apartmentRepository,
			HouseholdRepository householdRepository,
			com.water.water.service.WaterUsageService waterUsageService,
			com.water.water.service.OnboardingService onboardingService,
			com.water.water.service.DemoDataSeeder demoDataSeeder) {
		return args -> java.util.concurrent.CompletableFuture.runAsync(() -> {
			try {
				// Wait for Hibernate ddl-auto schema creation to complete with retry
				if (!waitForSchema(dataSource)) {
					return; // Schema never became ready — skip all bootstrap tasks
				}

				List<User> users = userRepository.findAll();
				for (User user : users) {
					if (!user.isApproved()) {
						user.setApproved(true);
						userRepository.save(user);
						System.out.println("Auto-approved existing user on startup: " + user.getEmail());
					}
				}

				// Automatically fix legacy consumption data on startup
				int fixed = waterUsageService.recalculateAllConsumption();
				System.out.println("Bootstrap: Recalculated consumption for " + fixed + " logs.");

				// Drop legacy/orphaned tables left over from old schema versions
				onboardingService.dropLegacyTables();

				// Seed meter readings for Harsh for last 6 months up to 3 Aug 2026
				String seedMsg = waterUsageService.seedMeterReadingsForHarsh();
				System.out.println("Bootstrap: " + seedMsg);

				// Seed 3 Community Admins, Residents, 3 Months Water Logs, and Paid Bills up to July
				String demoMsg = demoDataSeeder.seedDemoData();
				System.out.println("Bootstrap: " + demoMsg);
			} catch (Exception e) {
				System.err.println("Background bootstrap error (app is still healthy and running): " + e.getMessage());
			}
		});
	}
}


