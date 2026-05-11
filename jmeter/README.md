# Exercise A — JMeter

Files:

- `plan.jmx` — JMeter test plan (Thread Group: 50 users, 30s ramp-up; HTTP sampler to https://jsonplaceholder.typicode.com/posts/1; Response Assertion checks for `userId`; Summary Report listener)

Run headless (from the `jmeter` folder):

```bash
# Linux/macOS/Windows PowerShell (JMeter must be on PATH)
jmeter -n -t plan.jmx -l results.jtl
```

Notes:

- After the run `results.jtl` will contain sample rows. To open a GUI summary, run `jmeter -g results.jtl -o report` or open the `.jtl` in JMeter GUI.
