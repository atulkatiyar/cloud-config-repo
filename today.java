@PutMapping("/log4j2.xml")
    public ResponseEntity<TimerResponse> generateLog4j(
            @RequestParam(name = "rootLevel") String rootLevel,
            @RequestParam(name = "packageLogLevels", required = false) String packageLogLevels,
            @RequestParam(name = "timer") String timer, Authentication authentication) {
            Map<String, Object> data = new HashMap<>();
            long cacheExpirationTime = Long.parseLong(timer);
            long cacheExpirationTime1 = TimeUnit.SECONDS.toMillis(cacheExpirationTime);
            Timer timer1 = new Timer(System.currentTimeMillis(), System.currentTimeMillis() + cacheExpirationTime1);

            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, PackageInfo> loggingLevelsMap;
            try {
                loggingLevelsMap = objectMapper.readValue(packageLogLevels, HashMap.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
