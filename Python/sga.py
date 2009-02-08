import random

class sgaLocus:
	"""
	Class for defining allele position (locus) in genome
	"""
	def __init__(self, Alleles):
		self.Genes = [None, None, None]  # [FirstGenome, SecondGenome, BestGenome]
		self.Alleles = dict([(x, 1.0/len(Alleles)) for x in Alleles])

class sgaGenotype:
	"""
	Class for defining population genotype.
	Main class for evolving problem solutions
	"""
	def __init__(self, alleleGroups, Amount, genomeCompareFunc):
		self.genomeCompareFunc = genomeCompareFunc
		self.AlleleAffectValue = 0.005
		self.MutationProbability = 1.0/(Amount * len(alleleGroups))
		self.LocusGroups = [[sgaLocus(algr) for algr in alleleGroups] for x in range(Amount)]
		# Initializing random generators
		self.randmut = random.Random()
		self.randsel = random.Random()
		self.randfrq = random.Random()
	
	# >>> private methods starts
	
	def __GenerateIndividual(self, GenomeNo):
		assert GenomeNo in [0,1]
		for group in self.LocusGroups:
			for locus in group:
				al = None
				# Does mutation occur
				if self.randmut.random() < self.MutationProbability:
					al = self.randsel.choice(locus.Alleles.keys())
				# select allele by it`s frequency in genotype
				else:
					while not al:
						for allele in locus.Alleles.keys():
							if self.randfrq.random() < locus.Alleles[allele]:
								al = allele
								break
				# allele is selected, now setting genome
				locus.Genes[GenomeNo] = al
				if locus.Genes[2] == None:
					locus.Genes[2] = al
	
	def __AffectAlleles(self, BetterGenome, UpdateBest):
		assert BetterGenome in [0,1]
		afirst  = self.AlleleAffectValue if BetterGenome == 0 else -self.AlleleAffectValue
		asecond = self.AlleleAffectValue if BetterGenome == 1 else -self.AlleleAffectValue
		
		for group in self.LocusGroups:
			for locus in group:
				pfirst  = locus.Alleles[locus.Genes[0]] + afirst
				psecond = locus.Alleles[locus.Genes[1]] + asecond
				pfirst  = 1.0 if pfirst > 1.0  else 0.0 if pfirst < 0.0  else pfirst
				psecond = 1.0 if psecond > 1.0 else 0.0 if psecond < 0.0 else psecond
				locus.Alleles[locus.Genes[0]] = pfirst
				locus.Alleles[locus.Genes[1]] = psecond
				# check do we need to update best genome
				if UpdateBest:
					locus.Genes[2] = locus.Genes[BetterGenome]
	
	# <<< private methods ends
	
	def Evolve(self,cycles):
		for iter in range(cycles):
			self.__GenerateIndividual(0)
			self.__GenerateIndividual(1)
			BetterGenome,UpdateBest = self.genomeCompareFunc(self)
			if BetterGenome:
				self.__AffectAlleles(BetterGenome,UpdateBest)
	
	def DumpGenotype(self):
		for ixg,group in enumerate(self.LocusGroups):
			for ixl,locus in enumerate(group):
				for ixa,allele in enumerate(locus.Alleles):
					print "Grp_"+str(ixg),"|","Loc_"+str(ixl),"|",allele,"=>",locus.Alleles[allele],"prob."

def testSGA(gen):
	# Try to find x,y,z such that equation x^2 - y^2 - z^2 - 27 = 0
	diff0 = abs(gen.LocusGroups[0][0].Genes[0]**2 - gen.LocusGroups[1][0].Genes[0]**2 - gen.LocusGroups[2][0].Genes[0]**2 - 27)
	diff1 = abs(gen.LocusGroups[0][0].Genes[1]**2 - gen.LocusGroups[1][0].Genes[1]**2 - gen.LocusGroups[2][0].Genes[1]**2 - 27)
	diffb = abs(gen.LocusGroups[0][0].Genes[2]**2 - gen.LocusGroups[1][0].Genes[2]**2 - gen.LocusGroups[2][0].Genes[2]**2 - 27)
	better = None
	update = None
	if diff0 < diff1:
		better = 0
		update = True if diff0 < diffb else False
	elif diff1 < diff0:
		better = 1
		update = True if diff1 < diffb else False
	
	return better,update

if __name__ == "__main__":
	print 'Solving equation x^2 - y^2 - z^2 - 27 = 0'
	gen = sgaGenotype([range(2,61)], 3, testSGA)
	gen.Evolve(5000)
	print 'Best try:       ',str(gen.LocusGroups[0][0].Genes[2])+'^2 - '+str(gen.LocusGroups[1][0].Genes[2])+'^2 - '+str(gen.LocusGroups[2][0].Genes[2])+'^2 - 27 =' \
		,gen.LocusGroups[0][0].Genes[2]**2 - gen.LocusGroups[1][0].Genes[2]**2 - gen.LocusGroups[2][0].Genes[2]**2 - 27
	
