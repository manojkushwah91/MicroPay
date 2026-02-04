# Database Configuration Issues Analysis

## 🔴 CRITICAL ISSUES FOUND

### Issue #1: Secret Key Mismatch in Kubernetes Deployments

**Problem:**
- Kubernetes deployments reference secret keys that **DO NOT EXIST**:
  - Deployments try to read: `POSTGRES_USERNAME` and `POSTGRES_PASSWORD`
  - But secrets file only has: `POSTGRES_USER` and `POSTGRES_PASSWORD`

**Affected Files:**
- `infrastructure/k8s/wallet-service.yaml` (lines 28, 33)
- `infrastructure/k8s/payment-service.yaml` (lines 28, 33)
- `infrastructure/k8s/transaction-service.yaml` (lines 28, 33)
- `infrastructure/k8s/notification-service.yaml` (lines 28, 33)

**Current Secret Keys:**
```yaml
# micropay-secrets.yaml
POSTGRES_USER: micropay
POSTGRES_PASSWORD: micropay_pass
SPRING_DATASOURCE_USERNAME: micropay
SPRING_DATASOURCE_PASSWORD: Manoj@8719895574
```

**What Kubernetes Tries to Read:**
```yaml
# All service deployments
secretKeyRef:
  name: micropay-secrets
  key: POSTGRES_USERNAME  # ❌ DOES NOT EXIST
  key: POSTGRES_PASSWORD   # ✅ EXISTS
```

**Impact:** 
- `SPRING_DATASOURCE_USERNAME` will be **null/empty**
- Service will crash with: `Could not resolve placeholder 'POSTGRES_USER'` or authentication failure

---

### Issue #2: Environment Variable Name Mismatch

**Problem:**
- Config-repo files expect: `${POSTGRES_USER}` and `${POSTGRES_PASSWORD}`
- Kubernetes sets: `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`
- **These don't match!**

**Config-Repo Files Expect:**
```yaml
# config-repo/auth-service.yml
spring:
  datasource:
    username: ${POSTGRES_USER}      # Expects env var: POSTGRES_USER
    password: ${POSTGRES_PASSWORD}  # Expects env var: POSTGRES_PASSWORD
```

**Kubernetes Sets:**
```yaml
# All service deployments
env:
  - name: SPRING_DATASOURCE_USERNAME  # Sets: SPRING_DATASOURCE_USERNAME
  - name: SPRING_DATASOURCE_PASSWORD  # Sets: SPRING_DATASOURCE_PASSWORD
```

**Impact:**
- Config Server tries to resolve `${POSTGRES_USER}` from environment
- Environment has `SPRING_DATASOURCE_USERNAME` instead
- **Placeholder resolution fails** → Service crashes at startup

---

### Issue #3: Database URL Variable Mismatch

**Problem:**
- Config-repo files expect service-specific DB URL variables
- ConfigMaps set `SPRING_DATASOURCE_URL` (which won't resolve in config-repo)

**Config-Repo Files Expect:**
```yaml
# auth-service.yml
url: ${AUTH_DB_URL}

# wallet-service.yml  
url: ${WALLET_DB_URL}

# payment-service.yml
url: ${PAYMENT_DB_URL}

# transaction-service.yml
url: ${TRANSACTION_DB_URL}

# notification-service.yml
url: ${NOTIFICATION_DB_URL}
```

**ConfigMaps Set:**
```yaml
# All service configmaps
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/micropay_db
```

**Impact:**
- Config Server cannot resolve `${AUTH_DB_URL}`, `${WALLET_DB_URL}`, etc.
- Service crashes with: `Could not resolve placeholder 'AUTH_DB_URL'`

---

### Issue #4: No Default Values for Critical Variables

**Problem:**
- All config-repo files use `${VARIABLE}` without default values
- If variable is missing → **immediate crash**

**Examples:**
```yaml
# ❌ NO DEFAULT - Will crash if missing
password: ${POSTGRES_PASSWORD}

# ✅ SHOULD BE - With default fallback
password: ${POSTGRES_PASSWORD:defaultpassword}
```

**Impact:**
- Any missing environment variable causes immediate startup failure
- No graceful degradation or error messages

---

### Issue #5: Inconsistent Variable Naming

**Problem:**
- Multiple naming conventions used:
  - `POSTGRES_USER` vs `POSTGRES_USERNAME`
  - `SPRING_DATASOURCE_USERNAME` vs `POSTGRES_USER`
  - `AUTH_DB_URL` vs `SPRING_DATASOURCE_URL`

**Impact:**
- Confusion and mismatches
- Hard to maintain
- Easy to introduce bugs

---

## 🔧 ROOT CAUSE SUMMARY

1. **Secret Key Names Don't Match**: Deployments reference `POSTGRES_USERNAME` but secret has `POSTGRES_USER`
2. **Env Var Names Don't Match**: Config expects `POSTGRES_USER` but K8s sets `SPRING_DATASOURCE_USERNAME`
3. **DB URL Variables Missing**: Config expects `${SERVICE_DB_URL}` but K8s sets `SPRING_DATASOURCE_URL`
4. **No Fallback Values**: Missing variables cause immediate crashes
5. **Config Server Resolution**: Config Server reads from config-repo, which references env vars that K8s doesn't set correctly

---

## 💥 WHAT HAPPENS WHEN SERVICES START

### Startup Sequence:
1. Service starts → Loads config from Config Server
2. Config Server reads `config-repo/{service}.yml`
3. Config Server tries to resolve `${POSTGRES_USER}` from environment
4. Environment has `SPRING_DATASOURCE_USERNAME` (wrong name)
5. **Placeholder resolution fails**
6. Spring Boot throws: `IllegalArgumentException: Could not resolve placeholder 'POSTGRES_USER'`
7. **Service crashes immediately**

### Error Messages You'll See:
```
Caused by: java.lang.IllegalArgumentException: 
  Could not resolve placeholder 'POSTGRES_USER' in value "${POSTGRES_USER}"
  
Caused by: java.lang.IllegalArgumentException: 
  Could not resolve placeholder 'AUTH_DB_URL' in value "${AUTH_DB_URL}"
```

---

## ✅ FIXES REQUIRED

### Fix #1: Update Secret Keys
Add missing keys to `micropay-secrets.yaml`:
```yaml
POSTGRES_USERNAME: micropay  # Add this
```

### Fix #2: Update Kubernetes Deployments
Change all deployments to set correct env var names:
```yaml
env:
  - name: POSTGRES_USER  # Change from SPRING_DATASOURCE_USERNAME
    valueFrom:
      secretKeyRef:
        name: micropay-secrets
        key: POSTGRES_USER  # Change from POSTGRES_USERNAME
  - name: POSTGRES_PASSWORD
    valueFrom:
      secretKeyRef:
        name: micropay-secrets
        key: POSTGRES_PASSWORD
```

### Fix #3: Add DB URL Variables
Add service-specific DB URL env vars to each deployment:
```yaml
env:
  - name: AUTH_DB_URL
    value: "jdbc:postgresql://postgres:5432/micropay_db"
  - name: WALLET_DB_URL
    value: "jdbc:postgresql://postgres:5432/micropay_db"
  # ... etc
```

### Fix #4: Add Default Values (Optional but Recommended)
Update config-repo files with defaults:
```yaml
username: ${POSTGRES_USER:defaultuser}
password: ${POSTGRES_PASSWORD:defaultpass}
url: ${AUTH_DB_URL:jdbc:postgresql://localhost:5432/micropay_db}
```

---

## 📋 CHECKLIST FOR EACH SERVICE

For each service (auth, wallet, payment, transaction, notification):

- [ ] Secret key exists: `POSTGRES_USER` (not `POSTGRES_USERNAME`)
- [ ] Deployment sets: `POSTGRES_USER` env var (not `SPRING_DATASOURCE_USERNAME`)
- [ ] Deployment sets: `POSTGRES_PASSWORD` env var
- [ ] Deployment sets: `{SERVICE}_DB_URL` env var (e.g., `AUTH_DB_URL`)
- [ ] Config-repo file uses: `${POSTGRES_USER}` (matches env var name)
- [ ] Config-repo file uses: `${POSTGRES_PASSWORD}` (matches env var name)
- [ ] Config-repo file uses: `${SERVICE_DB_URL}` (matches env var name)

---

## 🎯 QUICK FIX PRIORITY

1. **HIGH**: Fix secret key references in all deployments
2. **HIGH**: Fix env var names to match config-repo expectations
3. **HIGH**: Add DB URL env vars to all deployments
4. **MEDIUM**: Add default values to prevent crashes
5. **LOW**: Standardize naming conventions



