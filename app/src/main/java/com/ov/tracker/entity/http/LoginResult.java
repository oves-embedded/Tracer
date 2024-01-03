package com.ov.tracker.entity.http;


import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LoginResult implements Serializable {
    /**
     * data
     */
    @SerializedName("data")
    private DataDTO data;

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public static class DataDTO {
        /**
         * signInUser
         */
        @SerializedName("signInUser")
        private SignInUserDTO signInUser;

        public SignInUserDTO getSignInUser() {
            return signInUser;
        }

        public void setSignInUser(SignInUserDTO signInUser) {
            this.signInUser = signInUser;
        }

        public static class SignInUserDTO {
            /**
             * id
             */
            @SerializedName("_id")
            private String id;
            /**
             * accessToken
             */
            @SerializedName("accessToken")
            private String accessToken;
            /**
             * actionScope
             */
            @SerializedName("actionScope")
            private Object actionScope;
            /**
             * agentId
             */
            @SerializedName("agentId")
            private Object agentId;
            /**
             * agentType
             */
            @SerializedName("agentType")
            private Object agentType;
            /**
             * authenticationInstance
             */
            @SerializedName("authenticationInstance")
            private AuthenticationInstanceDTO authenticationInstance;
            /**
             * birthDate
             */
            @SerializedName("birthDate")
            private Object birthDate;
            /**
             * createdAt
             */
            @SerializedName("createdAt")
            private String createdAt;
            /**
             * deleteAt
             */
            @SerializedName("deleteAt")
            private Object deleteAt;
            /**
             * deleteStatus
             */
            @SerializedName("deleteStatus")
            private Boolean deleteStatus;
            /**
             * email
             */
            @SerializedName("email")
            private String email;
            /**
             * firstName
             */
            @SerializedName("firstName")
            private Object firstName;
            /**
             * hireDate
             */
            @SerializedName("hireDate")
            private Object hireDate;
            /**
             * idString
             */
            @SerializedName("idString")
            private Object idString;
            /**
             * idType
             */
            @SerializedName("idType")
            private Object idType;
            /**
             * lastName
             */
            @SerializedName("lastName")
            private Object lastName;
            /**
             * name
             */
            @SerializedName("name")
            private String name;
            /**
             * officeAddress
             */
            @SerializedName("officeAddress")
            private Object officeAddress;
            /**
             * profile
             */
            @SerializedName("profile")
            private Object profile;
            /**
             * role
             */
            @SerializedName("role")
            private AuthenticationInstanceDTO role;
            /**
             * roleName
             */
            @SerializedName("roleName")
            private String roleName;
            /**
             * subrole
             */
            @SerializedName("subrole")
            private Object subrole;
            /**
             * type
             */
            @SerializedName("type")
            private String type;
            /**
             * updatedAt
             */
            @SerializedName("updatedAt")
            private String updatedAt;
            /**
             * typename
             */
            @SerializedName("__typename")
            private String typename;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getAccessToken() {
                return accessToken;
            }

            public void setAccessToken(String accessToken) {
                this.accessToken = accessToken;
            }

            public Object getActionScope() {
                return actionScope;
            }

            public void setActionScope(Object actionScope) {
                this.actionScope = actionScope;
            }

            public Object getAgentId() {
                return agentId;
            }

            public void setAgentId(Object agentId) {
                this.agentId = agentId;
            }

            public Object getAgentType() {
                return agentType;
            }

            public void setAgentType(Object agentType) {
                this.agentType = agentType;
            }

            public AuthenticationInstanceDTO getAuthenticationInstance() {
                return authenticationInstance;
            }

            public void setAuthenticationInstance(AuthenticationInstanceDTO authenticationInstance) {
                this.authenticationInstance = authenticationInstance;
            }

            public Object getBirthDate() {
                return birthDate;
            }

            public void setBirthDate(Object birthDate) {
                this.birthDate = birthDate;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
            }

            public Object getDeleteAt() {
                return deleteAt;
            }

            public void setDeleteAt(Object deleteAt) {
                this.deleteAt = deleteAt;
            }

            public Boolean getDeleteStatus() {
                return deleteStatus;
            }

            public void setDeleteStatus(Boolean deleteStatus) {
                this.deleteStatus = deleteStatus;
            }

            public String getEmail() {
                return email;
            }

            public void setEmail(String email) {
                this.email = email;
            }

            public Object getFirstName() {
                return firstName;
            }

            public void setFirstName(Object firstName) {
                this.firstName = firstName;
            }

            public Object getHireDate() {
                return hireDate;
            }

            public void setHireDate(Object hireDate) {
                this.hireDate = hireDate;
            }

            public Object getIdString() {
                return idString;
            }

            public void setIdString(Object idString) {
                this.idString = idString;
            }

            public Object getIdType() {
                return idType;
            }

            public void setIdType(Object idType) {
                this.idType = idType;
            }

            public Object getLastName() {
                return lastName;
            }

            public void setLastName(Object lastName) {
                this.lastName = lastName;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Object getOfficeAddress() {
                return officeAddress;
            }

            public void setOfficeAddress(Object officeAddress) {
                this.officeAddress = officeAddress;
            }

            public Object getProfile() {
                return profile;
            }

            public void setProfile(Object profile) {
                this.profile = profile;
            }

            public AuthenticationInstanceDTO getRole() {
                return role;
            }

            public void setRole(AuthenticationInstanceDTO role) {
                this.role = role;
            }

            public String getRoleName() {
                return roleName;
            }

            public void setRoleName(String roleName) {
                this.roleName = roleName;
            }

            public Object getSubrole() {
                return subrole;
            }

            public void setSubrole(Object subrole) {
                this.subrole = subrole;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUpdatedAt() {
                return updatedAt;
            }

            public void setUpdatedAt(String updatedAt) {
                this.updatedAt = updatedAt;
            }

            public String getTypename() {
                return typename;
            }

            public void setTypename(String typename) {
                this.typename = typename;
            }

            public static class AuthenticationInstanceDTO {
                /**
                 * id
                 */
                @SerializedName("_id")
                private String id;
                /**
                 * name
                 */
                @SerializedName("name")
                private String name;
                /**
                 * typename
                 */
                @SerializedName("__typename")
                private String typename;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getTypename() {
                    return typename;
                }

                public void setTypename(String typename) {
                    this.typename = typename;
                }
            }
        }
    }
}
