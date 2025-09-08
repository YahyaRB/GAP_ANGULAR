import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateChauffeurComponent } from './update-chauffeur.component';

describe('UpdateChauffeurComponent', () => {
  let component: UpdateChauffeurComponent;
  let fixture: ComponentFixture<UpdateChauffeurComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [UpdateChauffeurComponent]
    });
    fixture = TestBed.createComponent(UpdateChauffeurComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
