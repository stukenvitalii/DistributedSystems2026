from cryptography import x509
from cryptography.hazmat.primitives import hashes, serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from cryptography.hazmat.primitives.serialization import pkcs12
from datetime import datetime, timedelta
from pathlib import Path

specs = [
    ("auth-service", Path("auth-service/src/main/resources/tls/auth-keystore.p12")),
    ("data-service", Path("data-service/src/main/resources/tls/data-keystore.p12")),
]

for cn, path in specs:
    path.parent.mkdir(parents=True, exist_ok=True)
    key = rsa.generate_private_key(public_exponent=65537, key_size=2048)
    subject = issuer = x509.Name([
        x509.NameAttribute(x509.NameOID.COMMON_NAME, cn),
        x509.NameAttribute(x509.NameOID.ORGANIZATIONAL_UNIT_NAME, "Lab3"),
        x509.NameAttribute(x509.NameOID.ORGANIZATION_NAME, "DistributedSystems"),
        x509.NameAttribute(x509.NameOID.LOCALITY_NAME, "Moscow"),
        x509.NameAttribute(x509.NameOID.STATE_OR_PROVINCE_NAME, "Moscow"),
        x509.NameAttribute(x509.NameOID.COUNTRY_NAME, "RU"),
    ])
    cert = (
        x509.CertificateBuilder()
        .subject_name(subject)
        .issuer_name(issuer)
        .public_key(key.public_key())
        .serial_number(x509.random_serial_number())
        .not_valid_before(datetime.utcnow() - timedelta(days=1))
        .not_valid_after(datetime.utcnow() + timedelta(days=365 * 5))
        .add_extension(x509.BasicConstraints(ca=True, path_length=None), critical=True)
        .sign(private_key=key, algorithm=hashes.SHA256())
    )
    p12 = pkcs12.serialize_key_and_certificates(
        name=cn.encode(),
        key=key,
        cert=cert,
        cas=None,
        encryption_algorithm=serialization.BestAvailableEncryption(b"changeit"),
    )
    path.write_bytes(p12)
    print(f"Generated keystore for {cn} at {path}")

