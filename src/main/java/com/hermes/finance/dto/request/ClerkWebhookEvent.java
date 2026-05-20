package com.hermes.finance.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Payload recebido nos webhooks do Clerk.
 * Campos não mapeados são ignorados para compatibilidade com versões futuras do
 * Clerk.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClerkWebhookEvent {

    private String type;
    private Data data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {

        private String id;

        @JsonProperty("email_addresses")
        private List<EmailAddress> emailAddresses;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<EmailAddress> getEmailAddresses() {
            return emailAddresses;
        }

        public void setEmailAddresses(List<EmailAddress> emailAddresses) {
            this.emailAddresses = emailAddresses;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        /** Retorna o email primário ou o primeiro da lista. */
        public String getPrimaryEmail() {
            if (emailAddresses == null || emailAddresses.isEmpty())
                return null;
            return emailAddresses.stream()
                    .filter(EmailAddress::isPrimary)
                    .map(EmailAddress::getEmailAddress)
                    .findFirst()
                    .orElse(emailAddresses.get(0).getEmailAddress());
        }

        /** Retorna o nome completo concatenado. */
        public String getFullName() {
            String first = firstName != null ? firstName.trim() : "";
            String last = lastName != null ? lastName.trim() : "";
            String full = (first + " " + last).trim();
            return full.isBlank() ? "Usuário" : full;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EmailAddress {

        @JsonProperty("email_address")
        private String emailAddress;

        private boolean primary;

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }
    }
}
