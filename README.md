# Server_keyvalue_pair
📡 Distributed Key-Value Store (UDP, At-Most-Once Semantics)

A high-performance UDP-based client-server key-value store built in Java using Protocol Buffers, designed to handle unreliable networks while guaranteeing at-most-once execution semantics.

🚀 Overview

This project implements a custom request-reply protocol on top of UDP and a fault-tolerant key-value service. It is designed to simulate real-world distributed systems challenges such as:

Packet loss & duplication
Network unreliability
Memory constraints
Idempotency & deduplication

✨ Key Features
🔁 At-Most-Once Semantics
Guarantees each request is processed no more than once
Uses unique 16-byte message IDs
Implements a request cache to detect and replay duplicate requests

📦 Custom Request-Reply Protocol
Built on top of UDP using DatagramSocket
Encapsulates messages with:
messageID
payload
CRC32 checksum for integrity
Handles:
Packet corruption detection
Retransmissions (client-side)

⚡ Protocol Buffers Integration
Uses Google Protocol Buffers for efficient serialization
Clean separation between:
Transport layer (Message.Msg)
Application layer (KVRequest / KVResponse)

🧠 Memory-Constrained Design
Runs under 64MB JVM heap limit
Tracks memory usage manually
Enforces:
OUT_OF_SPACE errors when exceeding limits
Achieves efficient memory utilization (≥50% target)

🔄 Duplicate Request Handling
Detects repeated requests via messageID
Returns cached response instead of re-executing
Ensures correctness under:
Packet loss
Client retries
Network duplication

⚙️ How to Run

1️⃣ Compile

mvn clean compile

2️⃣ Start Server (with memory limit)

java -Xmx64m -cp target/classes:protobuf-java-3.21.12.jar server.RequestReplyServer

3️⃣ Run Client

java -cp target/classes:protobuf-java-3.21.12.jar client.ClientAppTestingLocalServer <key> <value>

👤 Author
John Wong
